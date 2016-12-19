package net.unicon.grouper.externalusers.hooks.grouper;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import net.unicon.grouper.externalusers.utils.ExternalUsersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * * DuplicateGroupIdCheck checks to see if the Group name (extension) has been used anywhere else in Grouper.
 */
public class DuplicateGroupIdCheck extends GroupHooks {
        private final static Logger logger = LoggerFactory.getLogger(DuplicateGroupIdCheck.class);

        /**
         * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(HooksContext, HooksGroupBean)
         */
        @Override
        public void groupPreInsert(final HooksContext hooksContext, final HooksGroupBean preInsertBean) {
            checkGrouperForIdDuplicates(preInsertBean.getGroup());
        }

        /**
         * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
         */
        @Override
        public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
            checkGrouperForIdDuplicates(preUpdateBean.getGroup());
        }

    /**
     * Checks the Grouper system for duplicate extensions (id)
     * @param group the group being submitted
     */
    protected void checkGrouperForIdDuplicates(final Group group) {
        if (!ExternalUsersUtils.isActiveGroup(group.getName())){
            return;
        }

        if (group.getName().contains(" ")) {
            throw new HookVeto("hook.veto.id.grouper.contains.spaces",
                    String.format(GrouperConfig.retrieveConfig().getProperty("custom.duplicateGrouperGroupId.errorMessage.containsSpaces", "Spaces not allowed in Group ID.")));
        }

        final String groupExtension = group.getExtension();

        //Hunt down anything that may have the id in it...
        final Set<Group> list = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateName(groupExtension);

        logger.debug("Searched for candidates, and found {} possibilities.", list.size());

        //The search returns matches of the id, name, etc., so we need to check each result for the specific match we want.
        for (final Group testGroup : list) {

            if (!group.getId().equalsIgnoreCase(testGroup.getId()) //skip this one since it is us... of course we will match. (should not happen on new groups)
                    &&  groupExtension.equalsIgnoreCase(testGroup.getExtension())
                    ) {
                logger.info("Found duplicate id ({}) in grouper when adding '{}' ({})", new Object[] {testGroup.getName(), groupExtension, group.getName()});
                throw new HookVeto("hook.veto.id.grouper.duplicate",
                        String.format(GrouperConfig.retrieveConfig().getProperty("custom.duplicateGrouperGroupId.errorMessage", "The desired group id (%s) already exists in Grouper as %s."), groupExtension, testGroup.getName()));

            } else {
                //This could happen when updating itself
                logger.debug("Found ({}) in Grouper when adding/updating {} ({}); allowing it", new Object[]{testGroup.getName(), groupExtension, group.getName()});
            }
        }
    }
}
