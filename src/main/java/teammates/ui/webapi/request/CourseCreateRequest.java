package teammates.ui.webapi.request;

/**
 * The create request for the course.
 */
public class CourseCreateRequest extends CourseBasicRequest {
    private String courseId;

    @Override
    public void validate() {
        super.validate();
        assertTrue(courseId != null, "Time zone should not be null");
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
