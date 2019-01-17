import org.arekbee.RCode
import org.gradle.api.Project
import org.gradle.internal.impldep.org.junit.Test

class RPluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'org.arekbee.gradle-R-plugin'
        assertTrue(project.tasks.restore instanceof RCode)
    }
}
