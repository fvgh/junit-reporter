package com.kncept.junit.reporter;

import static org.gradle.testfixtures.ProjectBuilder.builder;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.kncept.junit.reporter.gradle.TestHTMLReporterPlugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestHTMLReporterPluginTest {

	@Test
	public void taskIsAppliedToProject() {
		try {
			addExceptionType(NoClassDefFoundError.class);
			Project project = builder().build();
			project.getPlugins().apply(JavaPlugin.class);
			project.getPlugins().apply(TestHTMLReporterPlugin.class);

			Task task = project.getTasks().getByName("junitHtmlReport");
			assertNotNull(task);
		} catch (NoClassDefFoundError e) {
			// suppressed for Maven :(
		}
	}

	private <T extends Throwable> void addExceptionType(Class<T> type) throws T {
	}

}
