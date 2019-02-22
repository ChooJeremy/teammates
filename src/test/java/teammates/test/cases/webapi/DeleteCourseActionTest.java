package teammates.test.cases.webapi;

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.util.Const;
import teammates.ui.webapi.action.DeleteCourseAction;
import teammates.ui.webapi.action.JsonResult;
import teammates.ui.webapi.output.MessageOutput;

/**
 * SUT: {@link DeleteCourseAction}.
 */
public class DeleteCourseActionTest
        extends BaseActionTest<DeleteCourseAction> {

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.COURSE;
    }

    @Override
    protected String getRequestMethod() {
        return DELETE;
    }

    @Override
    @Test
    public void testExecute() throws Exception {

        InstructorAttributes instructor1OfCourse1 = typicalBundle.instructors.get("instructor1OfCourse1");
        String instructorId = instructor1OfCourse1.googleId;
        String courseId = instructor1OfCourse1.courseId;

        ______TS("Typical case, delete a soft-deleted course in Recycle Bin");

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, courseId,
        };

        loginAsInstructor(instructorId);
        logic.moveCourseToRecycleBin(courseId);
        assertEquals(courseId, logic.getSoftDeletedCourseForInstructor(instructor1OfCourse1).getId());

        DeleteCourseAction action = getAction(submissionParams);
        JsonResult result = getJsonResult(action);
        MessageOutput message = (MessageOutput) result.getOutput();

        assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        assertEquals("The course " + courseId + " has been permanently deleted.", message.getMessage());

    }

    @Test
    public void testExecute_notInRecycleBin_shouldPass() throws Exception {
        InstructorAttributes instructor1OfCourse1 = typicalBundle.instructors.get("instructor1OfCourse1");
        String instructorId = instructor1OfCourse1.googleId;
        String courseId = instructor1OfCourse1.courseId;

        ______TS("delete a course not in Recycle Bin");

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, courseId,
        };

        loginAsInstructor(instructorId);

        DeleteCourseAction action = getAction(submissionParams);
        JsonResult result = getJsonResult(action);
        MessageOutput message = (MessageOutput) result.getOutput();

        assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        assertEquals("The course " + courseId + " has been permanently deleted.", message.getMessage());

    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, "idOfTypicalCourse1",
        };

        verifyOnlyInstructorsCanAccess(submissionParams);
        verifyInaccessibleWithoutModifyCoursePrivilege(submissionParams);
    }
}
