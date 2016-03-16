package ninja.siden.sirusi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class SirusiHandlerTest {

	Request input;

	AmazonS3 s3;
	SirusiHandler target;

	private Context createContext() {
		LambdaLogger logger = s -> System.out.println(s);
		Context ctx = mock(Context.class);
		when(ctx.getLogger()).thenReturn(logger);
		return ctx;
	}

	@Before
	public void setUp() throws Exception {
		this.s3 = mock(AmazonS3.class);
		this.target = new SirusiHandler() {
			@Override
			AmazonS3 createS3() {
				return s3;
			};
		};
		this.input = TestUtils.parse("gateway.json", Request.class);
	}

	@Test
	public void testRegex() throws Exception {
		assertTrue(SirusiHandler.index.matcher("index.htm").matches());
		assertTrue(SirusiHandler.index.matcher("index.html").matches());
		assertTrue(SirusiHandler.index.matcher("index.htML").matches());
	}

	@Test
	public void testHash() throws Exception {
		String s = SirusiHandler.hash("aaa");
		assertTrue(s.endsWith("="));
	}

	@Test
	public void index() throws Exception {
		Context ctx = createContext();
		String content = "aaaaaaaaaaaaaa";
		ObjectMetadata meta = mock(ObjectMetadata.class);
		when(meta.getContentLength()).thenReturn(Long.valueOf(content.length()));
		S3Object obj = mock(S3Object.class);
		when(obj.getObjectMetadata()).thenReturn(meta);
		InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		when(obj.getObjectContent()).thenReturn(new S3ObjectInputStream(in, null));
		when(s3.getObject(anyString(), anyString())).thenReturn(obj);

		input.seed = "index.html";
		Response resp = target.handleRequest(input, ctx);
		assertEquals(content, resp.getContent());
	}

	@Test
	public void redirect() {
		Context ctx = createContext();

		assertEquals(input.getSeed(), "hoge.fuga@example.com");
		assertEquals(input.getType(), "gif");
		assertEquals(input.getSize(), 32);

		when(s3.putObject(anyString(), anyString(), anyObject(), anyObject())).thenReturn(null);
		try {
			target.handleRequest(input, ctx);
			fail();
		} catch (Redirect e) {
			assertTrue(e.getMessage().startsWith("http"));
		}
	}
}
