package fr.insalyon.creatis.vip.core.server.dao.mysql;

import fr.insalyon.creatis.devtools.MD5;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.TermsOfUse;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants.GROUP_ROLE;
import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.client.view.util.CountryCode;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
@Transactional
public class CoreDataInitializer extends JdbcDaoSupport {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TableInitializer tableInitializer;

    private Server server;
    private UserDAO userDAO;
    private GroupDAO groupDAO;
    private UsersGroupsDAO usersGroupsDAO;
    private TermsUseDAO termsUseDAO;

    @Autowired
    public CoreDataInitializer(
            GroupDAO groupDAO, DataSource dataSource, TableInitializer tableInitializer,
            Server server, UserDAO userDAO, UsersGroupsDAO usersGroupsDAO, TermsUseDAO termsUseDAO) {
        setDataSource(dataSource);
        this.tableInitializer = tableInitializer;
        this.userDAO = userDAO;
        this.server = server;
        this.usersGroupsDAO = usersGroupsDAO;
        this.termsUseDAO = termsUseDAO;
        this.groupDAO = groupDAO;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Order(10) // higher priority to create the vip core tables before the ones that references them
    public void onStartup() {
        logger.info("Configuring VIP core database.");
        initializeUserTables();
        initializeGroupTables();
        initializeAccountTables();
        initializeTermsOfUseTable();
    }

    private void initializeUserTables() {
        if (tableInitializer.createTable("VIPUsers",
                "email VARCHAR(255), "
                        + "next_email VARCHAR(255), "
                        + "pass VARCHAR(40), "
                        + "first_name VARCHAR(255), "
                        + "last_name VARCHAR(255), "
                        + "institution VARCHAR(255), "
                        + "code VARCHAR(40), "
                        + "confirmed BOOLEAN, "
                        + "folder VARCHAR(100), "
                        + "session VARCHAR(255), "
                        + "registration TIMESTAMP, "
                        + "last_login TIMESTAMP, "
                        + "level VARCHAR(50), "
                        + "country_code VARCHAR(2), "
                        + "max_simulations int(11), "
                        + "termsUse TIMESTAMP, "
                        + "lastUpdatePublications TIMESTAMP,"
                        + "failed_authentications int(11),"
                        + "account_locked BOOLEAN,"
                        + "apikey VARCHAR(255),"
                        + "PRIMARY KEY(email),"
                        + "UNIQUE KEY(first_name,last_name),"
                        + "UNIQUE KEY(apikey)")) {

            String folder = server.getAdminFirstName().toLowerCase() + "_"
                    + server.getAdminLastName().toLowerCase();

            try {
                userDAO.add(
                        new User(server.getAdminFirstName(),
                                server.getAdminLastName(),
                                server.getAdminEmail(),
                                null,
                                server.getAdminInstitution(),
                                MD5.get(server.getAdminPassword()),
                                true,
                                UUID.randomUUID().toString(), folder, "",
                                new Date(), new Date(), UserLevel.Administrator,
                                CountryCode.fr, 100, null,null,0,false));

            } catch (DAOException | NoSuchAlgorithmException | UnsupportedEncodingException ex) {
                logger.error("Error creating VIPUsers table", ex);
            }
        }
    }

    private void initializeGroupTables() {
        if (tableInitializer.createTable("VIPGroups",
                "groupname VARCHAR(50), "
                        + "public BOOLEAN, "
                        + "gridfile BOOLEAN DEFAULT 0, "
                        + "gridjobs BOOLEAN DEFAULT 0, "
                        + "PRIMARY KEY(groupname)")) {

            try {
                groupDAO.add(new Group(CoreConstants.GROUP_SUPPORT, false, true, true));
            } catch (DAOException ex) {
                logger.error("Error creating VIPGroups table", ex);
            }
        }


        if (tableInitializer.createTable("VIPUsersGroups",
                "email VARCHAR(255), "
                        + "groupname VARCHAR(100), "
                        + "role VARCHAR(30), "
                        + "PRIMARY KEY (email, groupname), "
                        + "FOREIGN KEY (email) REFERENCES VIPUsers(email) "
                        + "ON DELETE CASCADE ON UPDATE CASCADE, "
                        + "FOREIGN KEY (groupname) REFERENCES VIPGroups(groupname) "
                        + "ON DELETE CASCADE ON UPDATE CASCADE")) {
            try {
                usersGroupsDAO.add(server.getAdminEmail(),
                                CoreConstants.GROUP_SUPPORT,
                                GROUP_ROLE.Admin);
            } catch (DAOException ex) {
                logger.error("Error adding admin user to admin group", ex);
            }
        }
    }

    private void initializeAccountTables() {
        tableInitializer.createTable("VIPAccounts",
                "name VARCHAR(255), "
                        + "PRIMARY KEY (name)");

        tableInitializer.createTable("VIPAccountsGroups",
                "name VARCHAR(255), "
                        + "groupname VARCHAR(255), "
                        + "PRIMARY KEY (name, groupname), "
                        + "FOREIGN KEY (name) REFERENCES VIPAccounts(name) "
                        + "ON DELETE CASCADE ON UPDATE CASCADE, "
                        + "FOREIGN KEY (groupname) REFERENCES VIPGroups(groupname) "
                        + "ON DELETE CASCADE ON UPDATE CASCADE");
    }

    private void initializeTermsOfUseTable() {
        if (tableInitializer.createTable("VIPTermsOfuse",
                "idTermsOfuse INT(11) NOT NULL AUTO_INCREMENT, "
                        + "date TIMESTAMP NULL, "
                        + "PRIMARY KEY (idTermsOfuse)")) {
            try {
                java.util.Date today = new java.util.Date();
                termsUseDAO.add(
                        new TermsOfUse(new java.sql.Timestamp(today.getTime())));
            } catch (DAOException ex) {
                logger.error("Error creating VIPGroups table", ex);
            }
        }
    }
}
