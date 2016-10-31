package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GroupContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Subject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * results from retrieving results
 */
public class RetrieveGroupHelperResult {

    /**
     * group
     */
    private Group group;

    /**
     * get the group from the request
     *
     * @param request
     * @param requirePrivilege (view is automatic)
     *
     * @return the group finder result
     */
    public static RetrieveGroupHelperResult retrieveGroupHelper(HttpServletRequest request, Privilege requirePrivilege) {

        return retrieveGroupHelper(request, requirePrivilege, true);

    }

    /**
     * get the group from the request
     *
     * @param request
     * @param requirePrivilege (view is automatic)
     * @param errorIfNotFound will put an error on the screen if nothing passed in
     *
     * @return the group finder result
     */
    public static RetrieveGroupHelperResult retrieveGroupHelper(HttpServletRequest request, Privilege requirePrivilege, boolean errorIfNotFound) {

        //initialize the bean
        GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();

        RetrieveGroupHelperResult result = new RetrieveGroupHelperResult();

        GrouperSession grouperSession = GrouperSession.staticGrouperSession();

        GroupContainer groupContainer = grouperRequestContainer.getGroupContainer();

        Group group = null;

        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();


        String groupId = request.getParameter("groupId");
        String groupIndex = request.getParameter("groupIndex");
        String groupName = request.getParameter("groupName");

        boolean addedError = false;

        if (!StringUtils.isBlank(groupId)) {
            group = GroupFinder.findByUuid(grouperSession, groupId, false);
        }
        else if (!StringUtils.isBlank(groupName)) {
            group = GroupFinder.findByName(grouperSession, groupName, false);
        }
        else if (!StringUtils.isBlank(groupIndex)) {
            long idIndex = GrouperUtil.longValue(groupIndex);
            group = GroupFinder.findByIdIndexSecure(idIndex, false, null);
        }
        else {

            //if viewing a subject, and that subject is a group, just show the group screen
            Subject subject = UiV2Subject.retrieveSubjectHelper(request, false);
            if (subject != null && GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
                group = GroupFinder.findByUuid(grouperSession, subject.getId(), false);
            }
            else {
                if (errorIfNotFound) {
                    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                            TextContainer.retrieveFromRequest().getText().get("groupCantFindGroupId")));

                    addedError = true;
                }
            }
        }

        if (group != null) {
            groupContainer.setGuiGroup(new GuiGroup(group));
            boolean privsOk = true;

            if (requirePrivilege != null) {
                if (requirePrivilege.equals(AccessPrivilege.ADMIN)) {
                    if (!groupContainer.isCanAdmin()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToAdminGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
                else if (requirePrivilege.equals(AccessPrivilege.VIEW)) {
                    if (!groupContainer.isCanView()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToViewGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
                else if (requirePrivilege.equals(AccessPrivilege.READ)) {
                    if (!groupContainer.isCanRead()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToReadGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
                else if (requirePrivilege.equals(AccessPrivilege.OPTIN)) {
                    if (!groupContainer.isCanOptin()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptinGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
                else if (requirePrivilege.equals(AccessPrivilege.OPTOUT)) {
                    if (!groupContainer.isCanOptout()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToOptoutGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
                else if (requirePrivilege.equals(AccessPrivilege.UPDATE)) {
                    if (!groupContainer.isCanUpdate()) {
                        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                                TextContainer.retrieveFromRequest().getText().get("groupNotAllowedToUpdateGroup")));
                        addedError = true;
                        privsOk = false;
                    }
                }
            }

            if (privsOk) {
                result.setGroup(group);
            }

        }
        else {

            if (!addedError && (!StringUtils.isBlank(groupId) || !StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupIndex))) {
                result.setAddedError(true);
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiScreenAction.GuiMessageType.error,
                        TextContainer.retrieveFromRequest().getText().get("groupCantFindGroup")));
                addedError = true;
            }

        }
        result.setAddedError(addedError);

        //go back to the main screen, cant find group
        if (addedError && errorIfNotFound) {
            guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
                    "/WEB-INF/grouperUi2/index/indexMain.jsp"));
        }

        return result;
    }

    /**
     * group
     *
     * @return group
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     * group
     *
     * @param group1
     */
    public void setGroup(Group group1) {
        this.group = group1;
    }

    /**
     * if added error to screen
     */
    private boolean addedError;

    /**
     * if added error to screen
     *
     * @return if error
     */
    public boolean isAddedError() {
        return this.addedError;
    }

    /**
     * if added error to screen
     *
     * @param addedError1
     */
    public void setAddedError(boolean addedError1) {
        this.addedError = addedError1;
    }
}
