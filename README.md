Grouper External Email/Users
============================
A modification to the Grouper UI that makes it easy for users to add external user to groups. Includes assisting hooks to limit group creation/editing if the name is on a restricted list.

## Use Cases
Grouper should be the system of record for mailing list. There is a need to add subscribers that aren't part of the normal campus subject source. Also these group's IDs needs to be restricted to not conflict with other identifiers. 

## Solution
For designated stems, this solution will:
 
1. Add functionality to the Grouper UI that enables group admins to add external users. They will fill in a first name, last name, and email address. The user will be added to a table in the Grouper schema/database, and then added to the group membership..
2. (optionally) Remove the external user from the database when theuser is removed from its last group membership.
3. (optionally) Prevent Group IDs from being duplicated if the IDs are already in use else where in Grouper.  
4. (optionally) Prevent Group Names from being duplicated if the names is already in use else where in Grouper.
5. (optionally) Prevent Group IDs from being used if they match something found in an alternate (i.e. user-defined) JDBC source.
6. (optionally) Prevent Group Names from being used if they match something found in an alternate (i.e. user-defined) JDBC source.

Grouper Admins can activate the optional features by specifying the appropriate hooks in the grouper.properties file(s).

## Requirements
This enhancement requires a functioning Grouper UI. The hooks, if used, should also be enabled in the Grouper Daemon and Grouper Web Services. 

## Build and Installation
External subjects are stored in a table in the Grouper database/schema. The general definition is:

```
CREATE TABLE custom_external_users (
    mail VARCHAR(100) NOT NULL, 
    givenName VARCHAR(40), 
    surname VARCHAR(40), 
    created_on BIGINT NOT NULL, 
    created_by VARCHAR(40) NOT NULL, 
    updated_on BIGINT, 
    updated_by VARCHAR(40), 
    PRIMARY KEY (mail)
);
```

The core code can be compiled by running `./gradlew jar`. The artifact library/jar will be found in `./build/libs/`. This jar needs to be placed in appropriate lib directory. For the daemon this will be `GROUPER_HOME/lib/custom`. For the the UI and WS, it is `TOMCAT_HOME/webapps/<app>/WEB-INF/lib/`. (It is anticipated that this will be applied to patched app directory.) 

`src/main/webapp/WEB-INF/grouperUi2/group/` contains a directory structure and two jsp files that need to be placed in the expanded Grouper UI webapp: `TOMCAT_HOME/webapps/grouper/WEB-INF/grouperUi2/group/`.

After updating the appropriate `sources.xml` and `grouper.properties`, and `grouper.hibernate.properties` files, a restart of the Daemon and Tomcat is needed for the changes to become effective.

## Execution
(There is nothing to directly execute.)

## Configuration

### sources.xml

A new subject source needs to be defined for the custom_external_uses table. A good starting sample is:
 
```
    <source adapterClass="edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter">
        <id>externalUsers</id>
        <name>External Users</name>
        <type>person</type>

        <init-param>
            <param-name>jdbcConnectionProvider</param-name>
            <param-value>edu.internet2.middleware.grouper.subj.GrouperJdbcConnectionProvider </param-value>
        </init-param>

        <init-param>
          <param-name>emailAttributeName</param-name>
          <param-value>mail</param-value>
        </init-param>

        <init-param>
            <param-name>maxResults</param-name>
            <param-value>100</param-value>
        </init-param>

        <init-param>
            <param-name>maxPageSize</param-name>
            <param-value>100</param-value>
        </init-param>

        <init-param>
            <param-name>SubjectID_AttributeType</param-name>
            <param-value>mail</param-value>
        </init-param>
        <init-param>
            <param-name>Name_AttributeType</param-name>
            <param-value>displayName</param-value>
        </init-param>
        <init-param>
            <param-name>Description_AttributeType</param-name>
            <param-value>description</param-value>
        </init-param>
        <init-param>
            <param-name>subjectVirtualAttribute_0_searchAttribute0</param-name>
            <param-value>${subject.name}</param-value>
        </init-param>
        <init-param>
            <param-name>sortAttribute0</param-name>
            <param-value>displayName</param-value>
        </init-param>
        <init-param>
            <param-name>searchAttribute0</param-name>
            <param-value>searchAttribute0</param-value>
        </init-param>

        <internal-attribute>searchAttribute0</internal-attribute>

        <init-param>
            <param-name>useInClauseForIdAndIdentifier</param-name>
            <param-value>true</param-value>
        </init-param>

        <!-- comma separate the identifiers for this row, this is for the findByIdentifiers if using an in clause -->
        <init-param>
            <param-name>identifierAttributes</param-name>
            <param-value>LOGINID</param-value>
        </init-param>

        <!-- subject identifier to store in grouper's member table -->
        <init-param>
            <param-name>subjectIdentifierAttribute0</param-name>
            <param-value>LOGINID</param-value>
        </init-param>

        <search>
            <searchType>searchSubject</searchType>
            <param>
                <param-name>sql</param-name>
                <param-value>
                    select
                    mail, trim(concat(givenName, ' ', surname, ' (', mail, ')')) as displayName
                    from
                    custom_external_users
                    where
                    {inclause}
                </param-value>
            </param>
            <param>
                <param-name>inclause</param-name>
                <param-value>
                    mail = ?
                </param-value>
            </param>
        </search>
        <search>
            <searchType>searchSubjectByIdentifier</searchType>
            <param>
                <param-name>sql</param-name>
                <param-value>
                    select
                    mail, trim(concat(givenName, ' ', surname, ' (', mail, ')')) as displayName
                    from
                    custom_external_users
                    where
                    {inclause}
                </param-value>
            </param>
            <param>
                <param-name>inclause</param-name>
                <param-value>
                    mail = ?
                </param-value>
            </param>
        </search>
        <search>
            <searchType>search</searchType>
            <param>
                <param-name>sql</param-name>
                <param-value>
                    select
                    mail, trim(concat(givenName, ' ', surname, ' (', mail, ')')) as displayName
                    from
                    custom_external_users
                    where
                    surname like CONCAT(?, '%')
                </param-value>
            </param>
        </search>
    </source>
```

### grouper.properties
Some of the functionality is controlled through hooks. Enable the following Grouper hooks for the desired functionality.

hooks.membership.class options:

- net.unicon.grouper.externalusers.hooks.RemoveUnusedExternalUser: Remove the external user from the database when theuser is removed from its last group membership.

hooks.group.class:

- net.unicon.grouper.externalusers.hooks.grouper.DuplicateGroupIdCheck: Prevent Group IDs from being duplicated if the IDs are already in use else where in Grouper.
- net.unicon.grouper.externalusers.hooks.grouper.DuplicateGroupNameCheck: Prevent Group Names from being duplicated if the names is already in use else where in Grouper.
- net.unicon.grouper.externalusers.hooks.jdbc.DuplicateGroupIdCheck: Prevent Group IDs from being used if they match something found in an alternate (i.e. user-defined) JDBC source.
- net.unicon.grouper.externalusers.hooks.jdbc.DuplicateGroupNameCheck: Prevent Group Names from being used if they match something found in an alternate (i.e. user-defined) JDBC source.


|Property Name|Default Value|Notes|
|-------------|-------------|-----|
|custom.externalusers.stem.[index]|(required)|Stems whose groups are allowed to have external members; index starts at 0 and must be sequential.|
|custom.externalusers.sourceId|(required)|The source id of the source's xml entry connect to the custom table; externalUsers in the sources.xml example above.|
|custom.duplicateJdbcGroupId.query|(required)|A query ran to find conflictsing group ids with an external JDBC datasource; the proposed id is passed in and a count is expected in return; a sample query: select count(*) from protected_group_ids where ID = ?
|custom.duplicateJdbcGroupName.query(required)|A query ran to find conflicting group names with an external JDBC datasource; the proposed id is passed in and a count is expected in return; a sample query:select count(*) from protected_group_names where name = ?|
|custom.duplicateJdbcGroupId.errorMessage|The desired group id (%s) already exists in the database.|%s will be substituted for the conflicting name/id.|
|custom.duplicateJdbcGroupName.errorMessage|The desired group name (%s) already exists in the database.|%s will be substituted for the conflicting name/id.|
|custom.duplicateGrouperGroupId.errorMessage|The desired group id (%s) already exists in Grouper as %s.|The first %s will be substituted for the conflicting name/id.The second %s will be the full conflicting Group Id.|
|custom.duplicateGrouperGroupName.errorMessage|The desired group name (%s) already exists in Grouper as %s.|The first %s will be substituted for the conflicting name/id. The second %s will be the full conflicting Group Id.|


An example configuration might looks like:

```
hooks.membership.class=net.unicon.grouper.externalusers.hooks.RemoveUnusedExternalUser
hooks.group.class=net.unicon.grouper.externalusers.hooks.jdbc.DuplicateGroupIdCheck,net.unicon.grouper.externalusers.hooks.grouper.DuplicateGroupIdCheck

########################################
## External Users Form Activation Stems
########################################
custom.externalusers.stem.0=cu:app:maillist
custom.externalusers.stem.1=cu:app:google

custom.externalusers.sourceId=externalUsers
        
# Queries of a third party system to prevent conflicting ids/names.
custom.duplicateJdbcGroupId.query=select count(*) from protected_group_names where name = ?
custom.duplicateJdbcGroupName.query=select count(*) from protected_group_names where name = ?

# %s will be substituted for the conflicting name/id.
custom.duplicateJdbcGroupId.errorMessage=The desired group id (%s) already exists in JDBC.
custom.duplicateJdbcGroupName.errorMessage=The desired group name (%s) already exists in JDBC.


## The second %s will be the conflicting Group Id.
custom.duplicateGrouperGroupId.errorMessage=The desired group id (%s) already exists in Grouper as %s.
custom.duplicateGrouperGroupName.errorMessage=The desired group name (%s) already exists in Grouper as %s.
```

### grouper.hibernate.properties
The `net.unicon.grouper.externalusers.hooks.jdbc.DuplicateGroupIdCheck` and `net.unicon.grouper.externalusers.hooks.jdbc.DuplicateGroupNameCheck` need information to connect to the external JDBC resource. This
  is done through the `grouper.hibernate.properties` file. Configuration properties match those of the standard database configuration except that properties are prefaced with `externalusers.`.

An example file is 
```
externalusers.hibernate.connection.url = jdbc:mysql://localhost:3306/idmTest

externalusers.hibernate.connection.username         = root

# Note: you can keep passwords external and encrypted: https://bugs.internet2.edu/jira/browse/GRP-122
externalusers.hibernate.connection.password         = supermanPassw0rd
```

## Local Development
This project has been supplemented with Docker. Docker's usage allows for quickly deploying the deployed artifact to a
consistent, repeatable, local Grouper environment, which facilitates consistent testing.

Docker (or docker-machine for Windows and OS X installations) should be locally installed. If using docker-machine is being used
the proper environment variables must be setup (i.e. those displayed by running `docker-machine env <vm name>`. `docker-machine ip <vm name>` will return the IP of the 

Running `gradle clean && gradle runGrouper` will compile the jar, build on top of the `unicon/grouper-demo` image (this could take 10-20 minutes
 the first time depending upon the network bandwidth available), and start an image. The image can be connected to from a browser by going to the port 8080. `docker ps` will display info about the running container. Running
 `docker exec -it dockercompose_grouper_1 bash` will allow one to connect into the running image. 

When testing is complete, `exit` to leave the running container. Then run `gradle clean` to clean
  the environment. Now you are ready to make the necessary code changes and start over again.

The following test work against this container:

1. Go into a regular folder and group, try to select "Add external user" from the menu. An error message should be displayed.
1. Go into `cu:app:maillist` or `cu:app:google` and create a group and add an external user. Filling in the fields should be successful. Leaving a field blank should cause a save error.
1. Trying to create group with the name of `testing` or `tester` in the stems listed in #2 should fail. But doing so out sides of these stems should succeed.
1. Trying to create group with the name of `allUsers` in the stems listed in #2 should fail. But doing so out sides of these stems should succeed, unless done in the `loader` stem..
