package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.subject.Subject;
import net.unicon.grouper.externalusers.data.DataAccess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UiV2GroupExternalUsers {

    private static final String ADD_EXTERNAL_USERS_JSP = "/WEB-INF/grouperUi2/group/groupAddExternalUsers.jsp";

    private static final String MAIN_CONTENT_DIV_ID = "#grouperMainContentDivId";

    /**
     * Custom form handling
     */
    @SuppressWarnings("unchecked")
    public void add(final HttpServletRequest request, HttpServletResponse response) {
        withCurrentGroupAndSession(request, new CurrentGroupAndSessionCallback() {

            @Override
            public void doWithGroupAndSession(Group group, GrouperSession session) {
                try {
                    if (group.isComposite()) {
                        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, "External users cannot be added to composite groups."));
                    }

                    if (isActiveGroup(group.getName())) {
                        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp(MAIN_CONTENT_DIV_ID, ADD_EXTERNAL_USERS_JSP));
                    } else {
                        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, "External users cannot be added to this group."));
                    }

                } catch(Exception ex) {
                    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, ex.getMessage()));
                }
            }
        });
    }


    /**
     * .
     *
     * @param request
     * @param response
     */
    public void addSubmit(final HttpServletRequest request, HttpServletResponse response) {
        withCurrentGroupAndSession(request, new CurrentGroupAndSessionCallback() {

            @Override
            public void doWithGroupAndSession(Group group, GrouperSession session) {
                StringBuilder sb = new StringBuilder();

                String mail = request.getParameter("mail");
                if (mail == null || mail.length() < 5) {
                    sb.append("<li>The e-mail address is required.</li>");
                }

                String givenName = request.getParameter("givenName");
                if (givenName == null || givenName.length() < 2) {
                    sb.append("<li>The first name is required.</li>");
                }

                String surname = request.getParameter("surname");
                if (surname == null || surname.length() < 2) {
                    sb.append("<li>The last name is required.</li>");
                }

                final String validationErrors = sb.toString();

                if (validationErrors.isEmpty()) {
                    if (DataAccess.externalUserExists(mail)) {
                        DataAccess.updateExternalUser(mail.trim(), givenName.trim(), surname.trim(), session.getSubject().getId());
                    } else {
                        DataAccess.createExternalUser(mail.trim(), givenName.trim(), surname.trim(), session.getSubject().getId());
                    }

                    Subject externalSubject = SubjectFinder.findById(mail.trim());
                    group.addMember(externalSubject, false);

                    //go to the view group screen
                    GuiResponseJs.retrieveGuiResponseJs().addAction(
                            GuiScreenAction.newScript(String.format("guiV2link('operation=UiV2Group.viewGroup&groupId=%s')", group.getId())));

                    //lets show a success message on the new screen
                    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.success, "User added."));

                    GrouperUserDataApi.recentlyUsedGroupAdd(GrouperUiUserData.grouperUiGroupNameForUserData(),
                            GrouperUiFilter.retrieveSubjectLoggedIn(), group);
                } else {
                    //Show validation errors.
                    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, String.format("Please fix these validation problems: <ul>%s</ul>", validationErrors)));
                }
            }
        });

    }

    /* Context switching objects, etc */
    private CurrentGroupAndSession currentGroupAndSession(HttpServletRequest req) {
        final GrouperSession grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());
        final Group group = RetrieveGroupHelperResult.retrieveGroupHelper(req, AccessPrivilege.UPDATE).getGroup();
        return new CurrentGroupAndSession(group, grouperSession);
    }

    /* Oh, java, just give me native tuples or simple structs values! */
    private static final class CurrentGroupAndSession {
        public final Group group;
        public final GrouperSession session;

        public CurrentGroupAndSession(Group group, GrouperSession session) {
            this.group = group;
            this.session = session;
        }
    }

    private void withCurrentGroupAndSession(HttpServletRequest request, CurrentGroupAndSessionCallback callback) {
        CurrentGroupAndSession currentGroupAndSession = null;
        try {
            currentGroupAndSession = currentGroupAndSession(request);
            if (currentGroupAndSession.group == null) {
                return;
            }
            callback.doWithGroupAndSession(currentGroupAndSession.group, currentGroupAndSession.session);
        }
        finally {
            if (currentGroupAndSession != null) {
                GrouperSession.stopQuietly(currentGroupAndSession.session);
            }
        }
    }

    private interface CurrentGroupAndSessionCallback {
        void doWithGroupAndSession(Group group, GrouperSession session);
    }


    private boolean isActiveGroup(String groupName) {
        String targetStem = null;

        int index = 0;

        while (true) {
            targetStem = GrouperConfig.retrieveConfig().propertyValueString("custom.externalUsers.stem." + index);

            if (targetStem == null || targetStem.isEmpty() ) {
                return false;
            }

            if (groupName.startsWith(targetStem)) {
                return true;
            }

            index++;
        }
    }
}
