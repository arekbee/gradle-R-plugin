import org.arekbee.RCode
import org.gradle.api.Project
import org.gradle.internal.impldep.org.junit.Test

class GreetingPluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'org.samples.greeting'

        assertTrue(project.tasks.build instanceof RCode)
        assertTrue(project.tasks.restore instanceof RCode)
    }
}
