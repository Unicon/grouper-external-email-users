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
 * * DuplicateGroupIdCheck checks to see if the Group name (extension) has been used anywhere else in Grouper.
 */
public class DuplicateGroupIdCheck extends GroupHooks {
        private final static Logger logger = LoggerFactory.getLogger(DuplicateGroupIdCheck.class);

        /**
         * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(HooksContext, HooksGroupBean)
         */
        @Override
        public void groupPreInsert(final HooksContext hooksContext, final HooksGroupBean preInsertBean) {
            checkJdbcForIdDuplicates(preInsertBean.getGroup());
        }

        /**
         * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
         */
        @Override
        public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
            checkJdbcForIdDuplicates(preUpdateBean.getGroup());
        }

    /**
     * Checks the Grouper system for duplicate extensions (id)
     * @param group the group being submitted
     */
    protected void checkJdbcForIdDuplicates(final Group group) {
        if (!ExternalUsersUtils.isActiveGroup(group.getName())){
            return;
        }

        String groupExtension = group.getExtension();

        SessionFactory sessionFactory = GenericDataAccess.getDatabaseSessionFactory();

        Session session = sessionFactory.openSession();
        List<Object> params = GrouperUtil.toList((Object)groupExtension);
        int count = GenericDataAccess.runSql(session, GrouperConfig.retrieveConfig().propertyValueStringRequired("custom.duplicateJdbcGroupId.query"), params);
        session.close();

        if (count > 0){
            logger.info("Found duplicate id ({}) in jdbc", new Object[]{groupExtension});
            throw new HookVeto("hook.veto.id.jdbc.duplicate", String.format(GrouperConfig.retrieveConfig().getProperty("custom.duplicateJdbcGroupId.errorMessage","The desired group id (%s) already exists in the database."), groupExtension));
        }
    }
}
