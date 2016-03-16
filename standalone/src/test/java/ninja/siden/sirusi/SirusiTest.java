/*
 * Copyright 2015 SATO taichi
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

import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import ninja.siden.App;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * @author taichi
 */
@RunWith(Theories.class)
public class SirusiTest {

	@DataPoints
	public static String[] inputs = { "9c7859bb1755d13722425c475b2d9a54",
			"5134f211ceaaae668e8456aae84c276e",
			"47606684ab6951fd942836fb8a35a30b" };

	Sirusi target;

	@Before
	public void setUp() throws Exception {
		this.target = new Sirusi(new App());
	}

	@Theory
	public void test(String input) throws Exception {
		Path path = Files.createTempFile(input, ".png");
		System.out.println(path);
		try (OutputStream fo = Files.newOutputStream(path)) {
			this.target.render(input, 48, "png", fo);
		}
		assertTrue(Files.exists(path));
		assertTrue(0 < path.toFile().length());
	}
}
