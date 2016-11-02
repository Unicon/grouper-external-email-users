package net.unicon.grouper.externalusers.data;

import edu.internet2.middleware.grouper.hibernate.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import java.util.List;

/**
 * Data Access for External Users
 *
 * Expects a table in the Grouper schema/database with the following definition:
 *  CREATE TABLE custom_external_users (mail VARCHAR(100) NOT NULL,
 *  givenName VARCHAR(40),
 *  surname VARCHAR(40),
 *  created_on BIGINT NOT NULL,
 *  created_by VARCHAR(40) NOT NULL,
 *  updated_on BIGINT,
 *  updated_by VARCHAR(40),
 *  PRIMARY KEY (mail));
 *
 *  (please alter as needed)
 */
public class GrouperDataAccess {

    /**
     * Checks to see if an external users already exists
     * @param mail the email adddress
     * @return true if found, otherwise false.
     */
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

    /**
     * Updates an existing user
     * @param mail
     * @param givenName
     * @param surname
     * @param subjectId the updating user's subject Id
     */
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

    /**
     * Adds a new user
     * @param mail
     * @param givenName
     * @param surname
     * @param subjectId the adding user's subject Id
     */
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

    /**
     * Remove a user
     * @param mail
     */
    public static void removeExternalUser(final String mail) {
        try {
            HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                /**
                 * callback
                 */
                @Override
                public Object callback(HibernateHandlerBean hibernateHandlerBean) {
                    List<Object> params = GrouperUtil.toList((Object)mail);
                    HibernateSession.bySqlStatic().executeSql("delete from custom_external_users where mail = ?", params);
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
