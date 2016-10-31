package net.unicon.grouper.externalusers.utils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/**
 * Misc methods.
 */
public class ExternalUsersUtils {

    /**
     * Checks a group name to see if it is in the consective list of the grouper.properties "custom.externalUsers.stem." properties.
     * @param groupName the groupName to check
     * @return true if it is found, otherwise false.
     */
    public static boolean isActiveGroup(String groupName) {
        String targetStem;

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
