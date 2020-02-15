/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forecastGenerator;
import com.rma.io.RmaFile;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import hec.ensemble.EnsembleTimeSeriesDatabase;
import hec2.wat.client.WatFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author WatPowerUser
 */
public class ForecastGeneratorAlternative extends SelfContainedPluginAlt{
    private List<DataLocation> _dataLocations = new ArrayList<>();
    private String _pluginVersion;
    private static final String DocumentRoot = "ForecastGeneratorAlternative";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;
    private double _maxVal;
    public ForecastGeneratorAlternative(){
        super();
        _dataLocations = new ArrayList<>();
    }
    public ForecastGeneratorAlternative(String name){
        this();
        setName(name);
    }
    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            if(_dataLocations!=null){
                saveDataLocations(root,_dataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;   
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
            }else{
                System.out.println("XML document root was imporoperly named.");
                return false;
            }
            if(_dataLocations==null){
                _dataLocations = new ArrayList<>();
            }
            _dataLocations.clear();
            loadDataLocations(ele, _dataLocations);
            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }
    public List<DataLocation> getOutputDataLocations(){
       //construct output data locations 
	return defaultDataLocations();
    }
    public List<DataLocation> getInputDataLocations(){
        //construct input data locations.
	return defaultDataLocations();
    }
    private List<DataLocation> defaultDataLocations(){
       	if(!_dataLocations.isEmpty()){
            return _dataLocations;
        }
        List<DataLocation> dlList = new ArrayList<>();
        //create a default location so that links can be initialized.
        DataLocation dloc = new DataLocation(this.getModelAlt(),_name,"Input Forecast");
        dlList.add(dloc);
	return dlList; 
    }
    public boolean setDataLocations(List<DataLocation> dataLocations){
        boolean retval = true;
        for(DataLocation dl : dataLocations){
            if(!_dataLocations.contains(dl)){
                DataLocation linkedTo = dl.getLinkedToLocation();
                if(linkedTo!=null){
                    if(validLinkedToDssPath(dl))
                    {
                        setModified(true);
                        _dataLocations.add(dl);
                        retval = true;
                    }                    
                }
            }else{
                DataLocation linkedTo = dl.getLinkedToLocation();
                if(linkedTo!=null){
                    if(validLinkedToDssPath(dl))
                    {
                        setModified(true);
                        retval = true;
                    }                    
                }
            }
        }
        if(retval)saveData();
	return retval;
    }
    private boolean validLinkedToDssPath(DataLocation dl){
        DataLocation linkedTo = dl.getLinkedToLocation();
        String dssPath = linkedTo.getDssPath();
        return !(dssPath == null || dssPath.isEmpty());
    }
    public void setComputeOptions(ComputeOptions opts){
        _computeOptions = opts;
    }
    @Override
    public boolean isComputable() {
        return true;
    }
    @Override
    public boolean compute() {
        if(_computeOptions instanceof hec2.wat.model.ComputeOptions){
            boolean returnValue = true;
            hec2.wat.model.ComputeOptions wco = (hec2.wat.model.ComputeOptions)_computeOptions;
            WatFrame fr = hec2.wat.WAT.getWatFrame();
            fr.addMessage("Computing Forecast Generator Alternative: " + _name);
            fr.addMessage("Simulation Time window is: " + wco.getSimulationTimeWindow().getTimeWindowString());
            fr.addMessage("Run Time window is: " + wco.getRunTimeWindow().getTimeWindowString());
            fr.addMessage("Event Time window is: " + wco.getEventList().get(wco.getCurrentEventNumber()).getTimeWindowString());
            fr.addMessage("Realization Number is: " + wco.getCurrentRealizationNumber());
            fr.addMessage("Lifecycle Number is: " + wco.getCurrentLifecycleNumber());
            fr.addMessage("Event Number is: " + wco.getCurrentEventNumber());
            fr.addMessage("Model Sequence number is: " + wco.getModelPosition());
            fr.addMessage("Run Directory is: " + wco.getRunDirectory());
            fr.addMessage("DSS File Path is:" + wco.getDssFilename());
            fr.addMessage("Compute Options Written To string Yeilds:");
            fr.addMessage(wco.toString());
            
            String inputPath = "";//this path should be based on the alternative, set by the user in the base WAT directory /FG folder.
            try {
                hec.ensemble.EnsembleTimeSeriesDatabase dbase = new hec.ensemble.JdbcEnsembleTimeSeriesDatabase(inputPath, false);
            } catch (Exception ex) {
                Logger.getLogger(ForecastGeneratorAlternative.class.getName()).log(Level.SEVERE, null, ex);
            }
            String outputPath = wco.getDssFilename();
            if(wco.isFrmCompute()){
                //stochastic
                //in this case, the data needs to be copied to the lifecycle directory based on the time window of the lifecycle?
                
                
            }else{
                //deterministic
                //in a deterministic compute, the file should be copied (based on the Time Window) without manipulation.
            }
            return returnValue;
        }
        //theoretically, this could mean it is a CWMS compute. 
        return false;
    }
    @Override
    public boolean cancelCompute() {
        return false;
    }
    @Override
    public String getLogFile() {
        return null;
    }
    @Override
    public int getModelCount() {
        return 1;
    }

}
