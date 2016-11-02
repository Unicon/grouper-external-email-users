package net.unicon.grouper.externalusers.utils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Misc methods.
 */
public class ExternalUsersUtils {
    private final static Logger logger = LoggerFactory.getLogger(ExternalUsersUtils.class);

    /**
     * Checks a group name to see if it is in the consective list of the grouper.properties "custom.externalusers.stem." properties.
     * @param groupName the groupName to check
     * @return true if it is found, otherwise false.
     */
    public static boolean isActiveGroup(String groupName) {
        String targetStem;

        int index = 0;

        while (true) {
            targetStem = GrouperConfig.retrieveConfig().propertyValueString("custom.externalusers.stem." + index);

            if (targetStem == null || targetStem.isEmpty() ) {
                return false;
            }

            if (groupName.startsWith(targetStem)) {
                return true;
            }

            index++;
        }
    }

    public static String getExternalSourceId(){
        return GrouperConfig.retrieveConfig().propertyValueStringRequired("custom.externalusers.sourceId");
    }
}
