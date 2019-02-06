import org.gradle.api.Project
import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject
import com.github.arekbee.RCode
import org.gradle.testfixtures.ProjectBuilder

class RCodeSpecification extends Specification {
    @Subject Task rcode = createRCode()

    def "simple rCode test"() {
        when:
        rcode.configure {
            interpreter = 'R2'
            expression = 'getwd'
        }

        then:
        rcode.group == 'rbase'
        rcode instanceof  RCode
        rcode.interpreter == 'R2'
        rcode.expression ==  'getwd'
    }


    protected Task createRCode() {
        final Project project = ProjectBuilder.builder().build()
        project.task('info', type: RCode)
    }
}