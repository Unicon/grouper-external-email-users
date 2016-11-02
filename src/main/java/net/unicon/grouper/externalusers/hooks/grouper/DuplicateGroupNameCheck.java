package net.unicon.grouper.externalusers.hooks.grouper;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import net.unicon.grouper.externalusers.utils.ExternalUsersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
/**
 * DuplicateGroupNameCheck checks to see if the Group displayName (displayExtension) has been used anywhere else in Grouper.
 */
public class DuplicateGroupNameCheck extends GroupHooks {
    private final static Logger logger = LoggerFactory.getLogger(DuplicateGroupNameCheck.class);

    /**
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(HooksContext, HooksGroupBean)
     */
    @Override
    public void groupPreInsert(final HooksContext hooksContext, final HooksGroupBean preInsertBean) {
        checkGrouperForNameDuplicates(preInsertBean.getGroup());
    }

    /**
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
     */
    @Override
    public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
        checkGrouperForNameDuplicates(preUpdateBean.getGroup());
    }

    /**
     * Checks the Grouper system for duplicate names
     * @param group the group being submitted
     */
    protected void checkGrouperForNameDuplicates(final Group group) {
        if (!ExternalUsersUtils.isActiveGroup(group.getName())){
            return;
        }

        //Hunt down anything that may have the name in it...
        final Set<Group> list = GrouperDAOFactory.getFactory().getGroup().findAllByAnyApproximateAttr(group.getDisplayExtension(), "", false);
        logger.debug("Searched for candidates, and found {} possibilities.", list.size());

        //The search returns matches of the id, name, etc., so we need to check each result for the specific match we want.
        for (final Group testGroup : list) {
            final String groupName = testGroup.getDisplayName();

            if (!group.getId().equalsIgnoreCase(testGroup.getId()) //skip this one since it is us... of course we will match. (should not happen on new groups)
                    && group.getDisplayExtension().equalsIgnoreCase(testGroup.getDisplayExtension())
                    ) {
                logger.warn("Found duplicate name ({}) in grouper when adding '{}' ({})", new Object[] {groupName, group.getDisplayExtension(), group.getDisplayName()});
                throw new HookVeto("hook.veto.name.grouper.duplicate", String.format(GrouperConfig.retrieveConfig().getProperty("custom.duplicateGrouperGroupName.errorMessage", "The desired group name (%s) already exists in Grouper as %s."), group.getDisplayExtension(), testGroup.getDisplayName()));

            } else {
                //This could happen when updating itself
                logger.debug("Found ({}) in Grouper when adding/updating {} ({}); allowing it", new Object[]{groupName, group.getDisplayExtension(), group.getDisplayName()});
            }
        }
    }
}
