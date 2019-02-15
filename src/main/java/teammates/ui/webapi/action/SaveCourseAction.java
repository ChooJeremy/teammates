package teammates.ui.webapi.action;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.ui.webapi.request.CourseSaveRequest;

/**
 * Save a course.
 */
public class SaveCourseAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }

        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, userInfo.id);
        CourseAttributes course = logic.getCourse(courseId);
        gateKeeper.verifyAccessible(instructor, course, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE);
    }

    @Override
    public ActionResult execute() {
        CourseSaveRequest courseSaveRequest = getAndValidateRequestBody(CourseSaveRequest.class);

        String courseId = courseSaveRequest.getCourseData().getCourseId();
        String courseName = courseSaveRequest.getCourseData().getCourseName();
        String courseTimeZone = courseSaveRequest.getCourseData().getTimeZone();

        try {
            logic.updateCourse(courseId, courseName, courseTimeZone);
        } catch (InvalidParametersException ipe) {
            return new JsonResult(ipe.getMessage(), HttpStatus.SC_BAD_REQUEST);
        } catch (EntityDoesNotExistException edee) {
            return new JsonResult(edee.getMessage(), HttpStatus.SC_NOT_FOUND);
        }

        return new JsonResult("Updated course [" + courseId + "] details: Name: " + courseName
                + ", Time zone: " + courseTimeZone, HttpStatus.SC_OK);
    }
}
