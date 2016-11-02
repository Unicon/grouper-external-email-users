package net.unicon.grouper.externalusers.hooks.jdbc;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import net.unicon.grouper.externalusers.data.GenericDataAccess;
import net.unicon.grouper.externalusers.utils.ExternalUsersUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        checkJdbcForNameDuplicates(preInsertBean.getGroup());
    }

    /**
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
     */
    @Override
    public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
        checkJdbcForNameDuplicates(preUpdateBean.getGroup());
    }

    /**
     * Checks the Grouper system for duplicate names
     * @param group the group being submitted
     */
    protected void checkJdbcForNameDuplicates(final Group group) {
        if (!ExternalUsersUtils.isActiveGroup(group.getName())){
            return;
        }

        String groupDisplayExtension = group.getDisplayExtension();

        SessionFactory sessionFactory = GenericDataAccess.getDatabaseSessionFactory();

        Session session = sessionFactory.openSession();
        List<Object> params = GrouperUtil.toList((Object)groupDisplayExtension);
        int count = GenericDataAccess.runSql(session, GrouperConfig.retrieveConfig().propertyValueStringRequired("custom.duplicateJdbcGroupName.query"), params);
        session.close();

        if (count > 0){
            logger.info("Found duplicate name ({}) in jdbc", new Object[]{groupDisplayExtension});
            throw new HookVeto("hook.veto.id.jdbc.duplicate", String.format(GrouperConfig.retrieveConfig().getProperty("custom.duplicateJdbcGroupName.errorMessage", "The desired group name (%s) already exists in the database."), groupDisplayExtension));
        }
    }
}
