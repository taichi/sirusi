package ninja.siden.sirusi;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

public class SirusiHandler implements RequestHandler<Request, Response> {

	static final Pattern index = Pattern.compile("index\\.htm[l]?", Pattern.CASE_INSENSITIVE);

	@Override
	public Response handleRequest(Request input, Context context) {
		Response res = new Response();
		try {
			if (input.getSeed().equalsIgnoreCase("favicon.ico")) {
				throw new Redirect(input.getImgroot() + "/favicon.ico");
			} else if (input.getSeed().length() < 1 || index.matcher(input.getSeed()).matches()) {
				AmazonS3 s3 = createS3();
				S3Object object = s3.getObject(input.getBucket(), "index.html");
				int length = (int) object.getObjectMetadata().getContentLength();
				byte[] bytes = new byte[length];
				object.getObjectContent().read(bytes);
				String content = new String(bytes, StandardCharsets.UTF_8);
				res.setContent(content);
				return res;
			} else {
				byte[] bytes = render(input.getSeed(), input.getSize(), input.getType());
				String filename = hash(input.getSeed()) + "_" + input.getSize() + "." + input.getType();

				ObjectMetadata om = new ObjectMetadata();
				om.setContentType(input.getContentType());
				om.setContentLength(bytes.length);
				Map<String, String> meta = new HashMap<>();
				meta.put("Seed", input.getSeed());
				om.setUserMetadata(meta);

				AmazonS3 s3 = createS3();
				s3.putObject(input.getBucket(), filename, new ByteArrayInputStream(bytes), om);
				throw new Redirect(input.getImgroot() + "/" + filename);
			}
		} catch (IOException | GeneralSecurityException e) {
			context.getLogger().log(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	static String hash(String seed) throws GeneralSecurityException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] bytes = md.digest(seed.getBytes(StandardCharsets.UTF_8));
		return Base64.getUrlEncoder().encodeToString(bytes);
	}

	AmazonS3 createS3() {
		return new AmazonS3Client();
	}

	public byte[] render(String seed, int size, String formatName) throws IOException {
		Random r = new Random(new BigInteger(seed.getBytes()).longValue());
		int[] kinds = { 5, 6, 7 };
		int boxel = kinds[Math.abs(r.nextInt()) % kinds.length];

		BufferedImage original = make(r, boxel);
		BufferedImage img = resize(boxel, original, size);

		ByteArrayOutputStream out = new ByteArrayOutputStream(48 * 48 * 2);
		ImageIO.write(img, formatName, out);
		return out.toByteArray();
	}

	BufferedImage make(Random r, int boxel) {
		BufferedImage img = square(boxel);
		int fa = Math.abs(r.nextInt());
		int ba = Color.WHITE.getRGB();
		int half = (boxel - (boxel % 2)) / 2 + 1;
		for (int x = 0; x < half; x++) {
			for (int y = 0; y < boxel; y++) {
				int col = r.nextBoolean() ? ba : fa;
				img.setRGB(x, y, col);
				img.setRGB(boxel - 1 - x, y, col);
			}
		}
		return img;
	}

	BufferedImage resize(int boxel, BufferedImage src, int size) {
		int mod = size % boxel;
		int scale = (size - mod) / boxel;
		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		BufferedImage out = square(size);
		Graphics2D g2d = out.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, size, size);
		g2d.dispose();

		int border = mod / 2;
		op.filter(src, out.getSubimage(border, border, out.getWidth() - border, out.getHeight() - border));
		return out;
	}

	BufferedImage square(int size) {
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_BGR);
	}
}
