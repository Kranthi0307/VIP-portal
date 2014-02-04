/* Copyright CNRS-CREATIS
 *
 * Rafael Ferreira da Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.core.server.dao.mysql;

import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.user.UserLevel;
import fr.insalyon.creatis.vip.core.client.view.util.CountryCode;
import fr.insalyon.creatis.vip.core.server.business.Server;
import fr.insalyon.creatis.vip.core.server.dao.CoreDAOFactory;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author Rafael Ferreira da Silva
 */
public class PlatformConnection {

    private final static Logger logger = Logger.getLogger(PlatformConnection.class);
    private static PlatformConnection instance;
    private boolean firstExecution;
    private Connection connection;

    public synchronized static PlatformConnection getInstance() throws DAOException {

        if (instance == null) {
            instance = new PlatformConnection();
        }
        return instance;
    }

    private PlatformConnection() throws DAOException {

        firstExecution = true;
        connect();
        createTables();
    }

    private void connect() {

        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/vip");
            connection = ds.getConnection();
            connection.setAutoCommit(true);

        } catch (SQLException ex) {
            logger.error(ex);
        } catch (NamingException ex) {
            logger.error(ex);
        }
    }

    private void createTables() {

        if (firstExecution) {
            logger.info("Configuring VIP database.");

            if (createTable("VIPUsers",
                    "email VARCHAR(255), "
                    + "pass VARCHAR(40), "
                    + "first_name VARCHAR(255), "
                    + "last_name VARCHAR(255), "
                    + "institution VARCHAR(255), "
                    + "phone VARCHAR(255), "
                    + "code VARCHAR(40), "
                    + "confirmed BOOLEAN, "
                    + "folder VARCHAR(100), "
                    + "session VARCHAR(255), "
                    + "registration TIMESTAMP, "
                    + "last_login TIMESTAMP, "
                    + "level VARCHAR(50), "
                    + "country_code VARCHAR(2), "
                    + "max_simulations int(11) "
                    + "PRIMARY KEY(email), UNIQUE KEY(first_name,last_name)")) {

                Server server = Server.getInstance();
                String folder = server.getAdminFirstName().toLowerCase() + "_"
                        + server.getAdminLastName().toLowerCase();

                try {
                    CoreDAOFactory.getDAOFactory().getUserDAO().add(
                            new User(server.getAdminFirstName(),
                            server.getAdminLastName(),
                            server.getAdminEmail(),
                            server.getAdminInstitution(),
                            server.getAdminPassword(),
                            server.getAdminPhone(), true,
                            UUID.randomUUID().toString(), folder, "",
                            new Date(), new Date(), UserLevel.Administrator,
                            CountryCode.fr, 100));

                } catch (DAOException ex) {
                    logger.error(ex);
                }
            }

            if (createTable("VIPGroups",
                    "groupname VARCHAR(50), "
                    + "public BOOLEAN, "
                    + "PRIMARY KEY(groupname)")) {

                try {
                    CoreDAOFactory.getDAOFactory().getGroupDAO().add(
                            new Group(CoreConstants.GROUP_SUPPORT, false,true,true));
                } catch (DAOException ex) {
                    logger.error(ex);
                }
            }

            createTable("VIPUsersGroups",
                    "email VARCHAR(255), "
                    + "groupname VARCHAR(100), "
                    + "role VARCHAR(30), "
                    + "PRIMARY KEY (email, groupname), "
                    + "FOREIGN KEY (email) REFERENCES VIPUsers(email) "
                    + "ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "FOREIGN KEY (groupname) REFERENCES VIPGroups(groupname) "
                    + "ON DELETE CASCADE ON UPDATE CASCADE");

            createTable("VIPAccounts",
                    "name VARCHAR(255), "
                    + "PRIMARY KEY (name)");

            createTable("VIPAccountsGroups",
                    "name VARCHAR(255), "
                    + "groupname VARCHAR(255), "
                    + "PRIMARY KEY (name, groupname), "
                    + "FOREIGN KEY (name) REFERENCES VIPAccounts(name) "
                    + "ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "FOREIGN KEY (groupname) REFERENCES VIPGroups(groupname) "
                    + "ON DELETE CASCADE ON UPDATE CASCADE");

            firstExecution = false;
        }

        //// tissues and physical parameters
//        createTable("Tissues",
//                "name VARCHAR(255), "
//                + "ontologyId INT,"
//                + "PRIMARY KEY (name)");
//
//        createTable("PhysicalProperties",
//                "tissueName VARCHAR(255),"
//                + "physicalPropertyId INT, "
//                + "author VARCHAR(255),"
//                + "comment VARCHAR(255),"
//                + "type VARCHAR(255),"
//                + "date DATE,"
//                + "PRIMARY KEY (physicalPropertyId)");
//
//        createTable("ChemicalBlend",
//                "physicalPropertyId INT,"
//                + "density DOUBLE,"
//                + "phase VARCHAR(255),"
//                + "PRIMARY KEY (physicalPropertyId)");
//
//        createTable("ChemicalComponents",
//                "physicalPropertyId INT,"
//                + "massPercentage DOUBLE,"
//                + "elementName VARCHAR(255),"
//                + " PRIMARY KEY (physicalPropertyId, elementName)");
//
//        createTable("MagneticProperties",
//                "physicalPropertyId INT,"
//                + "propertyName VARCHAR(255),"
//                + "distInstancId INT,"
//                + "PRIMARY KEY (physicalPropertyId, propertyName)");
//
//        createTable("MagneticPropertyNames",
//                "propertyName VARCHAR(255),"
//                + "PRIMARY KEY (propertyName)");
//
//        createTable("Echogenicities",
//                "physicalPropertyId INT,"
//                + "spatDistInstanceId INT,"
//                + "ampDistInstanceId INT,"
//                + "PRIMARY KEY (physicalPropertyId)");
//
//        ////distributions
//        createTable("Distribution",
//                "distributionName VARCHAR(255),"
//                + "expression VARCHAR(255),"
//                + "PRIMARY KEY (distributionName)");
//
//        createTable("DistributionParameters",
//                "distributionName VARCHAR(255),"
//                + "parameterName VARCHAR(255),"
//                + "symbol VARCHAR(255),"
//                + "PRIMARY KEY (distributionName,symbol)");
//
//        createTable("DistributionInstance",
//                "instanceid INT,"
//                + "distName VARCHAR(255),"
//                + "PRIMARY KEY (instanceid)");
//
//        createTable("DistributionInstanceValues",
//                "instanceid INT,"
//                + "parameterSymbol VARCHAR(255),"
//                + "value DOUBLE,"
//                + "PRIMARY KEY (instanceid,parameterSymbol)");
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Creates a table in the platform database.
     *
     * @param name Table name
     * @param columnsDefinition SQL syntax to define columns
     * @return
     */
    public boolean createTable(String name, String columnsDefinition) {

        try {
            Statement stat = connection.createStatement();
            stat.executeUpdate("CREATE TABLE " + name + " ("
                    + columnsDefinition + ") ENGINE=InnoDB");

            logger.info("Table " + name + " successfully created.");
            return true;

        } catch (SQLException ex) {
            if (!ex.getMessage().contains("already exists")) {
                logger.error(ex);
            }
            return false;
        }
    }
}