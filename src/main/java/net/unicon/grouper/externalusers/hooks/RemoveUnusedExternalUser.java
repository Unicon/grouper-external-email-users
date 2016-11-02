package net.unicon.grouper.externalusers.hooks;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.subject.Subject;
import net.unicon.grouper.externalusers.data.GrouperDataAccess;
import net.unicon.grouper.externalusers.hooks.grouper.DuplicateGroupIdCheck;
import net.unicon.grouper.externalusers.utils.ExternalUsersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Remove external users when they are no longer used in a membership.
 */
public class RemoveUnusedExternalUser extends MembershipHooks {
    private final static Logger logger = LoggerFactory.getLogger(DuplicateGroupIdCheck.class);

    @Override
    public void membershipPostRemoveMember(final HooksContext hooksContext, final HooksMembershipChangeBean postDeleteMemberBean) {
        Subject subject = postDeleteMemberBean.getMember().getSubject();
        logger.debug("Checking Subject ID: {}", subject.getId());

        if (!subject.getSourceId().equalsIgnoreCase(ExternalUsersUtils.getExternalSourceId())) {
            logger.debug("Source ID ({}) does not match {}; skipping hook.", subject.getSourceId(), ExternalUsersUtils.getExternalSourceId());
            return;
        }

        Member m = MemberFinder.findBySubject(hooksContext.grouperSession(), subject, false);
        Set<Membership> membershipSet = m.getMemberships();

        logger.debug("Subject ({}) has {} memberships.", subject.getId(), membershipSet.size());

        if (membershipSet.isEmpty() && GrouperDataAccess.externalUserExists(subject.getId())) {
            logger.info("Removing subject {} from external users", subject.getId());
            GrouperDataAccess.removeExternalUser(subject.getId());
        }
    }
}

