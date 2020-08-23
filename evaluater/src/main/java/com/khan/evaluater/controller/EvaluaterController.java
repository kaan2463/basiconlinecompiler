package com.khan.evaluater.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("evaluate")
public class EvaluaterController {
	File rootDir = new File("javaSources");

	public EvaluaterController() {
		if (!rootDir.exists()) {
			rootDir.mkdir();
		}
	}

	@PostMapping
	public String compile(@RequestBody Object codeMap) {
		@SuppressWarnings("rawtypes")
		Map map = (Map) codeMap;
		String codeString = (String) map.get("code");

		try {
			return compileRunAndTest(codeString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Error Occured When tring to run code!";
	}

	private String compileRunAndTest(String codeString)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		File sourceFile = new File(rootDir, "Solution.java");
		Files.write(sourceFile.toPath(), codeString.getBytes(StandardCharsets.UTF_8));

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(null, null, null, sourceFile.getPath());

		URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { rootDir.toURI().toURL() });
		Class<?> cls = Class.forName("Solution", true, classLoader); // Should print "hello".
		Object instance = cls.getDeclaredConstructor().newInstance(); // Should print "world".
		Method solutionMethod = cls.getMethod("solution", int.class);
		String result = applyTests(instance, solutionMethod);
		for (File file : rootDir.listFiles())
			if (!file.isDirectory())
				file.delete();
		return result;
	}

	private String applyTests(Object instance, Method solutionMethod)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String t0 = (String) solutionMethod.invoke(instance, 0);// 1
		String t1 = (String) solutionMethod.invoke(instance, 3);// 6
		String t2 = (String) solutionMethod.invoke(instance, 6);// 720
		String t3 = (String) solutionMethod.invoke(instance, 5);// 120
		String t4 = (String) solutionMethod.invoke(instance, 7);// 5040

		StringBuilder sb = new StringBuilder();

		sb.append("expected fact 0 = 1  result = ").append(t0).append(", ").append("1".equals(t0.trim())).append("\n");
		sb.append("expected fact 3 = 6  result = ").append(t1).append(", ").append("6".equals(t1.trim())).append("\n");
		sb.append("expected fact 6 = 720  result = ").append(t2).append(", ").append("720".equals(t2.trim()))
				.append("\n");
		sb.append("expected fact 5 = 120  result = ").append(t3).append(", ").append("120".equals(t3.trim()))
				.append("\n");
		sb.append("expected fact 7 = 5040  result = ").append(t4).append(", ").append("5040".equals(t4.trim()))
				.append("\n");

		return sb.toString();

	}

}
