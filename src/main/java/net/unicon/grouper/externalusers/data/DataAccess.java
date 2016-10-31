package net.unicon.grouper.externalusers.data;

import edu.internet2.middleware.grouper.hibernate.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import java.util.List;

/**
 * Created by jgasper on 10/31/16.
 */
public class DataAccess {
    /* Data layer interaction code */
    public static boolean externalUserExists(final String mail) {
        try {
            Boolean result = (Boolean)(HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                /**
                 * callback
                 */
                @Override
                public Object callback(HibernateHandlerBean hibernateHandlerBean) {
                    List<Object> params = GrouperUtil.toList((Object) mail);
                    int count = HibernateSession.bySqlStatic().select(int.class, "select count(mail) from custom_external_users where mail = ?", params);
                    return (count > 0);
                }
            }));

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void updateExternalUser(final String mail, final String givenName, final String surname, final String subjectId) {
        try {
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                /**
                 * callback
                 */
                @Override
                public Object callback(HibernateHandlerBean hibernateHandlerBean) {
                    List<Object> params = GrouperUtil.toList((Object)givenName, surname, System.currentTimeMillis(), subjectId, mail);
                    HibernateSession.bySqlStatic().executeSql("update custom_external_users set givenName = ?, surname = ?, updated_on = ?, updated_by = ? where mail = ?", params);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void createExternalUser(final String mail, final String givenName, final String surname, final String subjectId) {
        try {
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                /**
                 * callback
                 */
                @Override
                public Object callback(HibernateHandlerBean hibernateHandlerBean) {
                    List<Object> params = GrouperUtil.toList((Object)mail, givenName, surname, System.currentTimeMillis(), subjectId);
                    HibernateSession.bySqlStatic().executeSql("insert into custom_external_users (mail, givenName, surname, created_on, created_by) values (?, ?, ?, ?, ?)", params);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
