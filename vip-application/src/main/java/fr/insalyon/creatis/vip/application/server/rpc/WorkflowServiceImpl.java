/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.creatis.insa-lyon.fr/~silva
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
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
package fr.insalyon.creatis.vip.application.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.insalyon.creatis.vip.application.client.bean.Workflow;
import fr.insalyon.creatis.vip.application.client.bean.WorkflowInput;
import fr.insalyon.creatis.vip.application.client.rpc.WorkflowService;
import fr.insalyon.creatis.vip.application.server.business.WorkflowBusiness;
import fr.insalyon.creatis.vip.application.server.business.simulation.parser.InputParser;
import fr.insalyon.creatis.vip.application.server.dao.DAOFactory;
import fr.insalyon.creatis.vip.application.server.dao.derby.connection.JobsConnection;
import fr.insalyon.creatis.vip.common.server.ServerConfiguration;
import fr.insalyon.creatis.vip.common.server.dao.DAOException;
import fr.insalyon.creatis.vip.core.client.bean.Application;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rafael Silva
 */
public class WorkflowServiceImpl extends RemoteServiceServlet implements WorkflowService {

    @Override
    public List<Workflow> getWorkflows(String user, String application, String status, Date startDate, Date endDate) {
        try {
            if (endDate != null) {
                Calendar c1 = Calendar.getInstance();
                c1.setTime(endDate);
                c1.add(Calendar.DATE, 1);
                endDate = c1.getTime();
            }
            return DAOFactory.getDAOFactory().getWorkflowDAO().getList(user, application, status, startDate, endDate);

        } catch (DAOException ex) {
            return null;
        }
    }

    @Override
    public String getFile(String baseDir, String fileName) {
        try {
            FileReader fr = new FileReader(
                    ServerConfiguration.getInstance().getWorkflowsPath() + "/"
                    + baseDir + "/" + fileName);

            BufferedReader br = new BufferedReader(fr);

            String strLine;
            StringBuilder sb = new StringBuilder();

            while ((strLine = br.readLine()) != null) {
                sb.append(strLine);
                sb.append("\n");
            }

            br.close();
            fr.close();
            return sb.toString();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getFileURL(String baseDir, String fileName) {
        ServerConfiguration configuration = ServerConfiguration.getInstance();
        return "https://" + configuration.getApacheHost() + ":"
                + configuration.getApacheSSLPort()
                + "/workflows"
                + baseDir + "/" + fileName;
    }

    @Override
    public List<String>[] getApplicationsAndUsersList(String applicationClass) {
        try {
            fr.insalyon.creatis.vip.core.server.dao.DAOFactory daoFactory = fr.insalyon.creatis.vip.core.server.dao.DAOFactory.getDAOFactory();
            
            List<String> users = new ArrayList<String>();
            for (User user : daoFactory.getUserDAO().getUsers()) {
                users.add(user.getCanonicalName());
            }
            
            List<String> apps = new ArrayList<String>();
            for (Application app : daoFactory.getApplicationDAO().getApplications(applicationClass)) {
                apps.add(app.getName());
            }
            
            return new List[]{users, apps};
            
        } catch (DAOException ex) {
            return null;
        }
    }

    @Override
    public List<String> getLogs(String baseDir) {
        List<String> list = new ArrayList<String>();
        
        File folder = new File(ServerConfiguration.getInstance().getWorkflowsPath()
                + "/" + baseDir);
        
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                list.add(f.getName() + "-#-Folder");
            } else {
                String fileSize = f.length() + "";
                if (f.length() >= 1024) {
                    if (f.length() / 1024 >= 1024) {
                        fileSize = f.length() / 1024 / 1024 + " MB";
                    } else {
                        fileSize = f.length() / 1024 + " KB";
                    }
                }
                String info = f.getName() + "##" + fileSize
                        + "##" + new Date(f.lastModified());
                list.add(info + "-#-File");
            }
        }
        return list;
    }
    
    public List<String> getWorkflowSources(String user, String proxyFileName, String workflowName) {

        try {       
            WorkflowBusiness business = new WorkflowBusiness();
            return business.getWorkflowSources(user, proxyFileName, workflowName);

        } catch (BusinessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Map<String, String> getWorkflowInputs(String fileName) {
        return new InputParser().parse(fileName);
    }

    public String launchWorkflow(String user, Map<String, String> parametersMap,
            String workflowName, String proxyFileName) {
        
        try {
            WorkflowBusiness business = new WorkflowBusiness();
            return business.launch(user, parametersMap, workflowName, proxyFileName);
        
        } catch (BusinessException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String addWorkflowInput(String user, WorkflowInput workflowInput) {
        try {
            return DAOFactory.getDAOFactory().getWorkflowInputDAO().addWorkflowInput(user, workflowInput);
        } catch (DAOException ex) {
            return null;
        }
    }

    public List<WorkflowInput> getWorkflowsInputByUser(String user) {
        try {
            return DAOFactory.getDAOFactory().getWorkflowInputDAO().getWorkflowInputByUser(user);
        } catch (DAOException ex) {
            return null;
        }
    }
    
    public List<WorkflowInput> getWorkflowsInputByUserAndAppName(String user, String appName) {
        try {
            return DAOFactory.getDAOFactory().getWorkflowInputDAO().getWorkflowInputByUserAndAppName(user, appName);
        } catch (DAOException ex) {
            return null;
        }
    }

    public WorkflowInput getWorkflowInputByUserAndName(String user, String inputName) {
        try {
            return DAOFactory.getDAOFactory().getWorkflowInputDAO().getWorkflowInputByUserAndName(user, inputName);
        } catch (DAOException ex) {
            return null;
        }
    }

    public void removeWorkflowInput(String user, String inputName) {
        try {
            DAOFactory.getDAOFactory().getWorkflowInputDAO().removeWorkflowInput(user, inputName);
        } catch (DAOException ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection(String workflowID) {
        try {
            JobsConnection.getInstance().close(ServerConfiguration.getInstance().getWorkflowsPath() + "/" + workflowID + "/jobs.db");
        } catch (DAOException ex) {
            ex.printStackTrace();
        }
    }

    public List<String> getStats(List<Workflow> workflowIdList, int type, int binSize) {
        try {
            return DAOFactory.getDAOFactory().getWorkflowDAO().getStats(workflowIdList, type, binSize);
        } catch (DAOException ex) {
            return null;
        }

    }

    public void killWorkflow(String workflowID) {
        try {
            WorkflowBusiness business = new WorkflowBusiness();
            business.kill(workflowID);
        } catch (BusinessException ex) {
            ex.printStackTrace();
        }
    }

    public void cleanWorkflow(String workflowID, String userDN, String proxyFileName) {
        try {
            WorkflowBusiness business = new WorkflowBusiness();
            business.clean(workflowID, userDN, proxyFileName);
        } catch (BusinessException ex) {
            ex.printStackTrace();
        }
    }

    public void purgeWorkflow(String workflowID) {
        try {
            WorkflowBusiness business = new WorkflowBusiness();
            business.purge(workflowID);
        } catch (BusinessException ex) {
            ex.printStackTrace();
        }
    }
}