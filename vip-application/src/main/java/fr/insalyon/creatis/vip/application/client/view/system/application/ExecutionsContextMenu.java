package fr.insalyon.creatis.vip.application.client.view.system.application;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import fr.insalyon.creatis.vip.application.client.rpc.ReproVipService;
import fr.insalyon.creatis.vip.application.client.rpc.ReproVipServiceAsync;
import fr.insalyon.creatis.vip.application.client.view.monitor.ViewerWindow;
import fr.insalyon.creatis.vip.core.client.bean.Execution;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.ModalWindow;

public class ExecutionsContextMenu extends Menu {
    private ModalWindow modal;
    private String executionID;
    private ReproVipServiceAsync reproVipServiceAsync = ReproVipService.Util.getInstance();
    private ReproVipService reproVipService;
    public ExecutionsContextMenu(ModalWindow modal, String executionName, String executionID, String version){
        this.modal = modal;
        this.executionID = executionID;
        this.setShowShadow(true);
        this.setShadowDepth(10);
        this.setWidth(90);

        MenuItem OptionPublicExecutionItem = new MenuItem("Option make it public");
        OptionPublicExecutionItem.setIcon(CoreConstants.ICON_SUCCESS);
        OptionPublicExecutionItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent event) {
                createReproVipDirectory(executionName, executionID, version);
                //MakexExecutionPublic();
            }
        });

        MenuItem DownloadOutputDataItem = new MenuItem("Donwload outputs");
        DownloadOutputDataItem.setIcon(CoreConstants.ICON_INFO);
        DownloadOutputDataItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent event) {
                DownloadOutputDataExecution();
            }
        });

        this.setItems(OptionPublicExecutionItem, DownloadOutputDataItem);
    }
    private void MakexExecutionPublic() {
        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                modal.hide();
                SC.warn("Unable to make this execution public:<br />" + caught.getMessage());
            }
            @Override
            public void onSuccess(Void result) {
                modal.hide();
                SC.say("Execution made public successfully");
            }
        };
        modal.show("Make execution public", true);
        reproVipServiceAsync.updateExecution(executionID, "Public", callback);
    }
    private void DownloadOutputDataExecution() {
        final AsyncCallback<String> callback = new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                modal.hide();
                SC.warn("Unable to download outputs of this execution public:<br />" + caught.getMessage());
            }
            @Override
            public void onSuccess(String s) {
                modal.hide();
                new ViewerWindow("Execution Output Data", executionID, s).show();
            }
        };
        modal.show("Download Outputs", true);
        reproVipServiceAsync.downloadJsonOutputData(executionID, callback);
    }
    public void createReproVipDirectory(String executionName, String executionID, String version) {
        reproVipServiceAsync.createReproVipDirectory(executionName, executionID, version, new AsyncCallback<Void>() {
            public void onFailure(Throwable caught) {
                SC.warn("Error creating ReproVip directory: " + caught.getMessage());
            }
            public void onSuccess(Void result) {
                SC.say("ReproVip directory successfully created");
            }
        });
    }
}
