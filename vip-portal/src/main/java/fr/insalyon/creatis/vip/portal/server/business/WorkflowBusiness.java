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
package fr.insalyon.creatis.vip.portal.server.business;

import fr.insalyon.creatis.agent.vlet.client.VletAgentClient;
import fr.insalyon.creatis.agent.vlet.client.VletAgentClientException;
import fr.insalyon.creatis.agent.vlet.client.VletAgentPoolClient;
import fr.insalyon.creatis.vip.common.server.ServerConfiguration;
import fr.insalyon.creatis.vip.datamanagement.server.DataManagerUtil;
import fr.insalyon.creatis.vip.portal.client.bean.WorkflowDescriptor;
import fr.insalyon.creatis.vip.portal.server.business.simulation.ParameterSweep;
import fr.insalyon.creatis.vip.portal.server.business.simulation.WorkflowMoteurConfig;
import fr.insalyon.creatis.vip.portal.server.business.simulation.parser.GwendiaParser;
import fr.insalyon.creatis.vip.portal.server.business.simulation.parser.ScuflParser;
import fr.insalyon.creatis.vip.portal.server.dao.DAOException;
import fr.insalyon.creatis.vip.portal.server.dao.DAOFactory;
import fr.insalyon.creatis.vip.portal.server.dao.WorkflowDAO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.rpc.ServiceException;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

/**
 *
 * @author Rafael Silva
 */
public class WorkflowBusiness {

    /**
     * 
     * @param user
     * @param proxyFileName
     * @param workflowName
     * @return
     * @throws BusinessException
     */
    public List<String> getWorkflowSources(String user, String proxyFileName,
            String workflowName) throws BusinessException {

        try {
            WorkflowDescriptor wd = DAOFactory.getDAOFactory().getApplicationDAO().getApplication(workflowName);
            ServerConfiguration conf = ServerConfiguration.getInstance();
            URI uri = new URI("lfn://" + conf.getDataManagerLFCHost()
                    + ":" + conf.getDataManagerLFCPort()
                    + DataManagerUtil.parseBaseDir(user, wd.getLfn()));

            VletAgentClient client = new VletAgentClient(
                    ServerConfiguration.getInstance().getVletagentHost(),
                    ServerConfiguration.getInstance().getVletagentPort(),
                    proxyFileName);

            String workflowPath = client.getRemoteFile(uri.getPath(),
                    System.getenv("HOME") + "/.platform/workflows");

            if (workflowPath.endsWith(".gwendia")) {
                return new GwendiaParser().parse(workflowPath).getSources();
            } else {
                return new ScuflParser().parse(workflowPath).getSources();
            }

        } catch (URISyntaxException ex) {
            throw new BusinessException(ex);
        } catch (IOException ex) {
            throw new BusinessException(ex);
        } catch (SAXException ex) {
            throw new BusinessException(ex);
        } catch (VletAgentClientException ex) {
            throw new BusinessException(ex);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    /**
     * 
     * @param user
     * @param parametersMap
     * @param workflowName
     * @param proxyFileName
     * @return
     * @throws BusinessException
     */
    public String launch(String user, Map<String, String> parametersMap,
            String workflowName, String proxyFileName) throws BusinessException {

        try {
            String settings = "GRID=DIRAC\n"
                    + "SE=ccsrm02.in2p3.fr\n"
                    + "TIMEOUT=100000\n"
                    + "RETRYCOUNT=3\n"
                    + "MULTIJOB=1";

            List<ParameterSweep> parameters = new ArrayList<ParameterSweep>();
            for (String name : parametersMap.keySet()) {
                ParameterSweep ps = new ParameterSweep(name);
                String valuesStr = parametersMap.get(name);

                if (valuesStr.contains("##")) {
                    String[] values = valuesStr.split("##");
                    if (values.length != 3) {
                        throw (new ServiceException("Error in range"));
                    }
                    Double start = Double.parseDouble(values[0]);
                    Double stop = Double.parseDouble(values[1]);
                    Double step = Double.parseDouble(values[2]);

                    for (double d = start; d <= stop; d += step) {
                        ps.addValue(d + "");
                    }
                } else if (valuesStr.contains("@@")) {
                    String[] values = valuesStr.split("@@");
                    for (String v : values) {
                        ps.addValue(DataManagerUtil.parseBaseDir(user, v));
                    }
                } else {
                    ps.addValue(DataManagerUtil.parseBaseDir(user, valuesStr));
                }
                parameters.add(ps);
            }

            WorkflowDescriptor wd = DAOFactory.getDAOFactory().getApplicationDAO().getApplication(workflowName);
            String lfnPath = wd.getLfn().substring(wd.getLfn().lastIndexOf("/") + 1);
            String workflowPath = new File("").getAbsolutePath() + "/workflows/" + new File(lfnPath).getName();

            WorkflowMoteurConfig moteur = new WorkflowMoteurConfig(ServerConfiguration.getInstance().getMoteurServer(), workflowPath, parameters);
            moteur.setSettings(settings);
            String ws = moteur.launch(proxyFileName);

            return ws;

        } catch (RemoteException ex) {
            throw new BusinessException(ex);
        } catch (ServiceException ex) {
            throw new BusinessException(ex);
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }

    /**
     * 
     * @param workflowID
     * @throws BusinessException
     */
    public void kill(String workflowID) throws BusinessException {

        try {
            WorkflowMoteurConfig moteur = new WorkflowMoteurConfig(
                    ServerConfiguration.getInstance().getMoteurServer());
            moteur.kill(workflowID);

        } catch (RemoteException ex) {
            throw new BusinessException(ex);
        } catch (ServiceException ex) {
            throw new BusinessException(ex);
        }
    }

    /**
     *
     * @param workflowID
     * @param userDN 
     * @param proxyFileName 
     * @throws BusinessException
     */
    public void clean(String workflowID, String userDN, String proxyFileName) throws BusinessException {

        try {
            WorkflowDAO workflowDAO = DAOFactory.getDAOFactory().getWorkflowDAO();
            workflowDAO.updateStatus(workflowID, "Cleaned");
            String workflowsPath = ServerConfiguration.getInstance().getWorkflowsPath();
            File workflowDir = new File(workflowsPath + "/" + workflowID);

            for (File file : workflowDir.listFiles()) {
                if (!file.getName().equals("jobs.db")
                        && !file.getName().equals("workflow.out")
                        && !file.getName().equals("workflow.err")
                        && !file.getName().equals("gasw.log")) {
                    
                    FileUtils.deleteQuietly(file);
                }
            }
            List<String> outputs = workflowDAO.getOutputs(workflowID);
            VletAgentPoolClient client = new VletAgentPoolClient(
                    ServerConfiguration.getInstance().getVletagentHost(),
                    ServerConfiguration.getInstance().getVletagentPort(),
                    proxyFileName);

            for (String output : outputs) {
                client.delete(output, userDN);
            }
            workflowDAO.cleanWorkflow(workflowID);
            
        } catch (DAOException ex) {
            throw new BusinessException(ex);
        } catch (VletAgentClientException ex) {
            throw new BusinessException(ex);
        }
    }

    /**
     * 
     * @param workflowID
     * @throws BusinessException
     */
    public void purge(String workflowID) throws BusinessException {

        try {
            WorkflowDAO workflowDAO = DAOFactory.getDAOFactory().getWorkflowDAO();
            workflowDAO.delete(workflowID);

            String workflowsPath = ServerConfiguration.getInstance().getWorkflowsPath();
            File workflowDir = new File(workflowsPath + "/" + workflowID);
            FileUtils.deleteQuietly(workflowDir);

        } catch (DAOException ex) {
            throw new BusinessException(ex);
        }
    }
}
