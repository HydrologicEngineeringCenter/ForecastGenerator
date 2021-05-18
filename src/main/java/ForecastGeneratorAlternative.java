/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.io.RmaFile;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TimeSeriesIdentifier;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
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
    private static final String AlternativeDataset = "Input_Data";
    private ComputeOptions _computeOptions;
    private String _inputPath = "";
    private void setInputPath(String path){
        _inputPath = path;
    }
    private String getInputPath(){
        return _inputPath;
    }
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
            root.setAttribute(AlternativeDataset,getInputPath());
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
                if(ele.getAttribute(AlternativeDataset)!=null){
                setInputPath(ele.getAttributeValue(AlternativeDataset));
                }else{
                    setInputPath("");
                }
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
            String outputPath = changeExtension(wco.getDssFilename(),"db");
            //this should only happen once per lifecycle...
            if(!wco.isModelFirstTime()){ return returnValue;}
            if(wco.getCurrentEventNumber()!=1){
                fr.addMessage("Is model first compute and event number is not 1");
                java.io.File of = new java.io.File(outputPath);
                if(of.exists()){
                    
                    if(wco.shouldForceCompute()){
                        fr.addMessage("File exists, force compute is true, deleting old file");
                        of.delete();
                        
                    }else{
                        fr.addMessage("File exists, skipping");
                    }
                }
            }
            double multiplier = 0.0;
            if(wco.isFrmCompute()){
                //stochastic
                //in this case, the data needs to be copied to the lifecycle directory based on the time window of the lifecycle?
                multiplier = wco.getEventRandom();
                
            }else{
                //deterministic
                //in a deterministic compute, the file should be copied (based on the Time Window) without manipulation.
                multiplier = 10.0;
            }
            try {
                hec.JdbcTimeSeriesDatabase dbase = new hec.JdbcTimeSeriesDatabase(_inputPath, hec.JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
                List<hec.ensemble.TimeSeriesIdentifier> locations = dbase.getTimeSeriesIDs();
                ArrayList<EnsembleTimeSeries> etsList = new ArrayList<>();
                int count = 0;
                for(TimeSeriesIdentifier tsid: locations) {
                    System.out.println(tsid.toString());
                    EnsembleTimeSeries etsr = dbase.getEnsembleTimeSeries(tsid);
                    EnsembleTimeSeries modifiedEts = new EnsembleTimeSeries(tsid,
                            etsr.getUnits(),etsr.getDataType(),etsr.getVersion());
                    for( Ensemble e : etsr){
                        for(float[] v : e.getValues()){
                            for (int i = 0; i <v.length ; i++) {
                                v[i] = (float) (v[i]*multiplier);// offset by multiplier
                            }
                        }
                        modifiedEts.addEnsemble(e);
                        count ++;
                    }
                    etsList.add(modifiedEts);
                    fr.addMessage("Output Path " + outputPath);
                    hec.JdbcTimeSeriesDatabase dbaseOut = new hec.JdbcTimeSeriesDatabase(outputPath,hec.JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
                    dbaseOut.write(etsList.toArray(new hec.ensemble.EnsembleTimeSeries[0]));
                }
            } catch (Exception ex) {
                Logger.getLogger(ForecastGeneratorAlternative.class.getName()).log(Level.SEVERE, null, ex);
            }

            return returnValue;
        }
        //theoretically, this could mean it is a CWMS compute. 
        return false;
    }
    private String changeExtension(String f, String newExtension) {
        int i = f.lastIndexOf('.');
        String oldPathandName = f.substring(0,i);
        return oldPathandName + '.' + newExtension;
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
