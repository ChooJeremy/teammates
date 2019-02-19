package teammates.ui.webapi.action;

import java.time.ZoneId;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.ui.webapi.request.CourseSaveRequest;
import teammates.common.util.FieldValidator;

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
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);

        CourseSaveRequest courseSaveRequest = getAndValidateRequestBody(CourseSaveRequest.class);

        String courseName = courseSaveRequest.getCourseName();
        String courseTimeZone = courseSaveRequest.getTimeZone();

        FieldValidator validator = new FieldValidator();
        String timeZoneErrorMessage = validator.getInvalidityInfoForTimeZone(courseTimeZone);
        if (!timeZoneErrorMessage.isEmpty()) {
            return new JsonResult(timeZoneErrorMessage, HttpStatus.SC_BAD_REQUEST);
        }

        try {
            logic.updateCourseCascade(
                    CourseAttributes.updateOptionsBuilder(courseId)
                            .withName(courseName)
                            .withTimezone(ZoneId.of(courseTimeZone))
                            .build());
        } catch (InvalidParametersException ipe) {
            return new JsonResult(ipe.getMessage(), HttpStatus.SC_BAD_REQUEST);
        } catch (EntityDoesNotExistException edee) {
            return new JsonResult(edee.getMessage(), HttpStatus.SC_NOT_FOUND);
        }

        return new JsonResult("Updated course [" + courseId + "] details: Name: " + courseName
                + ", Time zone: " + courseTimeZone, HttpStatus.SC_OK);
    }
}
