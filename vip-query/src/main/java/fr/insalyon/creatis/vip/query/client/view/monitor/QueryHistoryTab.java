/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.insalyon.creatis.vip.query.client.view.monitor;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.VisibilityMode;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;

import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.viewer.DetailViewer;

import fr.insalyon.creatis.vip.core.client.view.ModalWindow;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;

import fr.insalyon.creatis.vip.query.client.rpc.QueryService;
import fr.insalyon.creatis.vip.query.client.view.ParameterValue;
import fr.insalyon.creatis.vip.query.client.view.QueryConstants;
import fr.insalyon.creatis.vip.query.client.view.QueryExecutionRecord;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *
 * @author Boujelben
 */
public class QueryHistoryTab extends Tab {
     
     public  SearchStackSection searchSection;
     protected ModalWindow modal;
     protected ListGrid grid;
     ListGridField linkField;
     protected String user = null;
     protected String status = null;
     protected Date startDate = null;
     protected Date endDate = null;
     private ListGridRecord rollOverRecord;
     private DetailViewer detailViewer ;
     private Label l;

    
    
    
    public QueryHistoryTab() {
         
         
         
        this.setTitle(Canvas.imgHTML(QueryConstants.ICON_QUERYHISTORY) + "Query History");
        this.setID(QueryConstants.TAB_QUERYHISTORY);
        this.setCanClose(true);
        this.setAttribute("paneMargin", 0);
         configureGrid();
         modal = new ModalWindow(grid);
        

        VLayout vLayout = new VLayout();
        vLayout.addMember(new HistoryToolStrip(modal));

        SectionStack sectionStack = new SectionStack();
        sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
        sectionStack.setAnimateSections(true);

        SectionStackSection gridSection = new SectionStackSection();
        gridSection.setCanCollapse(false);
        gridSection.setShowHeader(false);
        gridSection.addItem(grid);

         searchSection =new SearchStackSection();

        sectionStack.setSections(gridSection, searchSection);
       
        vLayout.addMember(sectionStack);

        this.setPane(vLayout);
        loadData();
        
        
        
     }
      private void configureGrid() {

        grid = new ListGrid(){
             @Override  
            protected Canvas getCellHoverComponent(Record record, Integer rowNum, Integer colNum) {  
               
                 
                detailViewer = new DetailViewer();
                
                detailViewer.setWidth(200); 
              /*DetailViewerField name=new DetailViewerField("name", "name");
              DetailViewerField type=new DetailViewerField("value", "value");
               detailViewer.setFields(name,type);*/
               
                Long executionID=record.getAttributeAsLong("queryExecutionID");
                
                //appel rpc
                final AsyncCallback <List<String[]>> callback = new AsyncCallback<List<String[]>>() {
                     @Override
                     public void onFailure(Throwable caught) {
                
                     Layout.getInstance().setWarningMessage("Unable to save Query Execution " + caught.getMessage());
                     }
                    
                     @Override
                    public void onSuccess(List<String[]> result) {
                           List<ParameterValue> dataList = new ArrayList<ParameterValue>(); ;
                         for(String[] s:result)
                         {
                       
                      // DetailViewerField name=new DetailViewerField(s[0], s[1]);
                       dataList.add(new ParameterValue(s[0],s[1]));
                       String n=s[0];
                       l=new Label(n);
                
                         }
                   detailViewer.setData(dataList.toArray(new ParameterValue[]{}));
                     Layout.getInstance().setWarningMessage("Unable to");
                          }
                    };
                   QueryService.Util.getInstance().getParameterValue(executionID, callback);
                     
                    
                        
               
               

                
                
                
                 return  l;
                
            }
        };
            
        grid.setWidth100();
        grid.setHeight100();
        grid.setShowAllRecords(false);
        grid.setShowRowNumbers(true);
        grid.setShowEmptyMessage(true);
        grid.setSelectionType(SelectionStyle.SIMPLE);
        grid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        grid.setEmptyMessage("<br>No data available.");
        linkField =new ListGridField("urlResult", "Result Data");
        linkField.setType(ListGridFieldType.LINK);
        linkField.setWidth(150);  
        linkField.setAlign(Alignment.CENTER);  
        linkField.setLinkText(Canvas.imgHTML(QueryConstants.ICON_LINK, 16, 16, "info", "align=center", null)); 
         ListGridField executionID=new ListGridField("queryExecutionID", "queryExecutionID");
        
        ListGridField version = new ListGridField("version", "Version");
        version.setWidth(60);
        ListGridField status = new ListGridField("status", "Status");
        status.setWidth(60);
        
        grid.setFields(
                executionID,
                new ListGridField("name", "Query Execution Name"),
                new ListGridField("query", "Query"),
                version,
                new ListGridField("executer", "Executer"),
                new ListGridField("dateExecution", "Execution Start Time"),
                status,
                linkField
                );
        
           executionID.setHidden(true);
            }
      
                
      
      
      
          public void loadData() {

        final AsyncCallback<List<String[]>> callback = new AsyncCallback<List<String[]>>() {
            @Override
            public void onFailure(Throwable caught) {
                modal.hide();
                Layout.getInstance().setWarningMessage("Unable to get list of queries Execution:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(List<String[]> result) {
                modal.hide();
                List<QueryExecutionRecord> dataList = new ArrayList<QueryExecutionRecord>();

                for (String[] q : result ) {
                   
                    
                    dataList.add(new QueryExecutionRecord (q[0],q[1],q[2],q[3],q[4],q[5],q[6],q[7]));
                    
                   /* if(q[5].equals("completed")){
                        linkField.setLinkText(Canvas.imgHTML(QueryConstants.ICON_TICK, 16, 16, "info", "align=center", null)); 
                    }
                    else if(q[5].equals("failed")){
                        linkField.setLinkText(Canvas.imgHTML(QueryConstants.ICON_WAIT, 16, 16, "info", "align=center", null));
                    }
                    else if(q[5].equals("waiting")){
                         linkField.setLinkText(Canvas.imgHTML(QueryConstants.ICON_FAIL, 16, 16, "info", "align=center", null));
                }
                * */
                }
                grid.setData(dataList.toArray(new QueryExecutionRecord[]{}));
               
               
              
               
                }
                    
                
            
        };
        
       
        modal.show("Loading queries execution...", true);
        QueryService.Util.getInstance().getQueryHistory(callback);
    }
          
         public void expandSearchSection() {
        this.searchSection.setExpanded(true);
    }
         public void setUser(String user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public ListGridRecord[] getGridSelection() {
         return grid.getSelectedRecords();
         
        
         
          }
    
}