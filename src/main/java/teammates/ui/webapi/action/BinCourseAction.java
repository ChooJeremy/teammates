package teammates.ui.webapi.action;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.ui.webapi.output.CourseData;

/**
 * Move a course to the recycle bin.
 */
public class BinCourseAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }

        String idOfCourseToBin = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        gateKeeper.verifyAccessible(logic.getInstructorForGoogleId(idOfCourseToBin, userInfo.id),
                logic.getCourse(idOfCourseToBin), Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_COURSE);
    }

    @Override
    public ActionResult execute() {
        String idOfCourseToBin = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        try {
            CourseAttributes courseAttributes = logic.getCourse(idOfCourseToBin);
            logic.moveCourseToRecycleBin(idOfCourseToBin);

            return new JsonResult(new CourseData(courseAttributes));
        } catch (EntityDoesNotExistException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
