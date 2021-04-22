import org.gradle.api.component.SoftwareComponentFactory

import javax.inject.Inject

interface Services {
    @Inject
    SoftwareComponentFactory getSoftwareComponentFactory()
}