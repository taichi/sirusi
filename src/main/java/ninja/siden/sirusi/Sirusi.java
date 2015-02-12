/*
 * Copyright 2014 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ninja.siden.sirusi;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.cache.CacheHandler;
import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.util.MimeMappings;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Renderer;
import ninja.siden.Request;
import ninja.siden.Response;
import ninja.siden.internal.Core;

import org.xnio.OptionMap;

/**
 * @author taichi
 */
public class Sirusi {

	public static void main(String[] args) {
		App app = new App() {
			@Override
			protected HttpHandler wrap(OptionMap config, HttpHandler handler) {
				DirectBufferCache cache = new DirectBufferCache(1024, 10,
						1024 * 1024 * 200);
				return new CacheHandler(cache, super.wrap(config, handler));
			}
		};
		new Sirusi(app);
		app.listen(port()).asShutdownHook();
	}

	static int port() {
		String port = Objects.toString(System.getenv("PORT"), "8080");
		if (Pattern.matches("\\d{1,5}", port)) {
			int i = Integer.parseInt(port);
			if (0 < i && i < 65536) {
				return i;
			}
		}
		return 8080;
	}

	Set<String> types;

	public Sirusi(App app) {
		app.get("/favicon.ico", (req, res) -> getClass().getClassLoader()
				.getResource("favicon.ico"));
		app.get("/:seed.:type", this::identicon);
		app.get("/", (req, res) -> "I'm running!! yey!");
		this.types = new HashSet<>(Arrays.asList(ImageIO
				.getWriterFileSuffixes()));
	}

	public Object identicon(Request req, Response res) throws Exception {
		String seed = req.params("seed").orElse("0000000");
		String type = req.params("type")
				.filter(s -> types.contains(s.toLowerCase())).orElse("png");
		int size = req.query("s").filter(s -> s.matches("\\d{1,3}"))
				.map(Integer::parseInt).filter(i -> i < 4097).orElse(48);

		MimeMappings mm = req.raw().getAttachment(Core.CONFIG)
				.get(Config.MIME_MAPPINGS);
		res.type(mm.getMimeType(type));

		return res.render(seed, Renderer.ofStream((p, os) -> {
			render(seed, size, type, os);
		}));
	}

	public void render(String seed, int size, String formatName,
			OutputStream out) throws IOException {
		Random r = new Random(new BigInteger(seed.getBytes()).longValue());
		int[] kinds = { 5, 6, 7 };
		int boxel = kinds[Math.abs(r.nextInt()) % 3];

		BufferedImage original = make(r, boxel);
		BufferedImage img = resize(boxel, original, size);

		ImageIO.write(img, formatName, out);
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
		AffineTransformOp op = new AffineTransformOp(at,
				AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		BufferedImage out = square(size);
		Graphics2D g2d = out.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, size, size);
		g2d.dispose();

		int border = mod / 2;
		op.filter(
				src,
				out.getSubimage(border, border, out.getWidth() - border,
						out.getHeight() - border));
		return out;
	}

	BufferedImage square(int size) {
		return new BufferedImage(size, size, BufferedImage.TYPE_INT_BGR);
	}
}
