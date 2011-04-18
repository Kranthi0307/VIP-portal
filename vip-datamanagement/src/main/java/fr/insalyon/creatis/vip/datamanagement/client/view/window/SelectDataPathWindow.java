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
package fr.insalyon.creatis.vip.datamanagement.client.view.window;

import com.gwtext.client.core.RegionPosition;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.form.FieldSet;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.BorderLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;
import fr.insalyon.creatis.vip.datamanagement.client.view.panel.SelectDataBrowserPanel;

/**
 *
 * @author Rafael Silva
 */
public class SelectDataPathWindow extends Window {

    private static SelectDataPathWindow instance;
    private SelectDataBrowserPanel browserPanel;
    private String refID;
    private FieldSet fieldSet;

    public static SelectDataPathWindow getInstance() {
        if (instance == null) {
            instance = new SelectDataPathWindow();
        }
        return instance;
    }

    private SelectDataPathWindow() {

        this.setTitle("VIP Select Data Path");
        this.setWidth(550);
        this.setHeight(500);
        this.setResizable(true);
        this.setMaximizable(true);
        this.setClosable(true);
        this.setLayout(new FitLayout());

        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());

        browserPanel = SelectDataBrowserPanel.getInstance();
        panel.add(browserPanel, new BorderLayoutData(RegionPosition.CENTER));

        this.add(panel);
    }

    public void configure(String refID, FieldSet fieldSet) {
        this.refID = refID;
        this.fieldSet = fieldSet;
    }

    public void display() {
        this.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public FieldSet getFieldSet() {
        return fieldSet;
    }

    public String getRefID() {
        return refID;
    }
}
