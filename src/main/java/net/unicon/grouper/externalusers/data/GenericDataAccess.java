package net.unicon.grouper.externalusers.data;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.BySqlStatic;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

/**
 * Misc methods.
 */
public class GenericDataAccess {
    private final static Logger logger = LoggerFactory.getLogger(GenericDataAccess.class);
    private final static SessionFactory sessionFactory;

    static {
        // Find the custom configuration file
        Properties p = GrouperHibernateConfig.retrieveConfig().properties();

        for (String propertyName : p.stringPropertyNames()) {
            if (propertyName.startsWith("externalusers.")) {
                p.setProperty(propertyName.replace("externalusers.", ""), p.getProperty(propertyName));
            }
        }

        //un-encrypt pass
        if (p.containsKey("hibernate.connection.password")) {
            String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
            p.setProperty("hibernate.connection.password", newPass);
        }

        String connectionUrl = StringUtils.defaultString(GrouperUtil.propertiesValue(p, "hibernate.connection.url"));
        p.setProperty("hibernate.connection.url", connectionUrl);

        {
            String dialect = StringUtils.defaultString(GrouperUtil.propertiesValue(p, "hibernate.dialect"));
            dialect = GrouperDdlUtils.convertUrlToHibernateDialectIfNeeded(connectionUrl, dialect);
            p.setProperty("hibernate.dialect", dialect);
        }

        {
            String driver = StringUtils.defaultString(GrouperUtil.propertiesValue(p, "hibernate.connection.driver_class"));
            driver = GrouperDdlUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driver);
            p.setProperty("hibernate.connection.driver_class", driver);
        }

        // And now load all configuration information
        Configuration config = new Configuration().addProperties(p);

        for (String property : config.getProperties().stringPropertyNames()) {
            if (property.startsWith("hibernate.")) {
                logger.debug(property + ": " + config.getProperties().getProperty(property));
            }
        }

        sessionFactory = config.buildSessionFactory();
    }

    public static SessionFactory getDatabaseSessionFactory() {
            return sessionFactory;
    }

    public static int runSql(final Session session, final String sql, final List<Object> params) {
        PreparedStatement preparedStatement = null;

        try {
            Connection connection = ((SessionImpl)session).connection();
            preparedStatement = connection.prepareStatement(sql);

            BySqlStatic.attachParams(preparedStatement, params);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int numberOfRows = rs.getInt(1);
                System.out.println("numberOfRows= " + numberOfRows);
                return numberOfRows;
            } else {
                System.out.println("error: could not get the record counts");
                throw new RuntimeException("Problem with query: " + sql);
            }

        } catch (Exception e) {
            throw new RuntimeException("Problem with query: " + sql, e);
        } finally {
            GrouperUtil.closeQuietly(preparedStatement);
        }
    }
}
