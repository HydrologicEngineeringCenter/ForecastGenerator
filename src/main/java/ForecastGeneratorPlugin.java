/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.rma.factories.NewObjectFactory;
import hec.data.Parameter;
import hec.model.OutputVariable;
import hec2.map.GraphicElement;
import hec2.model.DataLocation;
import hec2.model.ProgramOrderItem;
import hec2.plugin.action.EditAction;
import hec2.plugin.action.OutputElement;
import hec2.plugin.lang.ModelLinkingException;
import hec2.plugin.lang.OutputException;
import hec2.plugin.model.ModelAlternative;
import hec2.wat.client.WatFrame;
import hec2.wat.model.tracking.OutputPlugin;
import hec2.wat.model.tracking.OutputVariableImpl;
import hec2.wat.plugin.AbstractSelfContainedWatPlugin;
import hec2.wat.plugin.CreatableWatPlugin;
import hec2.wat.plugin.WatPluginManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author WatPowerUser
 */
public class ForecastGeneratorPlugin extends AbstractSelfContainedWatPlugin<ForecastGeneratorAlternative> implements CreatableWatPlugin, OutputPlugin  {
    public static final String PluginName = "Forecast Generator";
    private static final String _pluginVersion = "1.0.0";
    private static final String _pluginSubDirectory = "FG";
    private static final String _pluginExtension = ".fg";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ForecastGeneratorPlugin p = new ForecastGeneratorPlugin();
//        String outputPath = "C:/Temp/Ensembles/test.db";
//        java.io.File f = new java.io.File(outputPath);
//        if(f.exists()){
//            f.delete();
//        }
//        try {
//            hec.ensemble.JdbcEnsembleTimeSeriesDatabase dbaseOut = new hec.ensemble.JdbcEnsembleTimeSeriesDatabase(outputPath,true);
//        } catch (Exception ex) {
//            Logger.getLogger(ForecastGeneratorPlugin.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    public ForecastGeneratorPlugin(){
        super();
        setName(PluginName);
        setProgramOrderItem(new ProgramOrderItem(PluginName,
                "A plugin designed to mock up ensemble forecasts",
                false,1,"shortname","Images/fda/wsp.png"));
        WatPluginManager.register(this);
    }
    @Override
    protected String getAltFileExtension() {
        return _pluginExtension;
    }
    @Override
    public String getPluginDirectory() {
        return _pluginSubDirectory;
    }
    @Override
    public String getVersion() {
        return _pluginVersion;
    }
    @Override
    public boolean saveProject() {
        boolean success = true;
        for(ForecastGeneratorAlternative alt: _altList){
            if(!alt.saveData()){
                success = false;
                System.out.println("Alternative " + alt.getName() + " could not save");
            }
        }
        return success;
    }
    @Override
    protected ForecastGeneratorAlternative newAlternative(String string) {
        return new ForecastGeneratorAlternative(string);
    }
    @Override
    protected NewObjectFactory getAltObjectFactory() {
        return new ForecastAlternativeFactory(this);
    }
    @Override
    public List<DataLocation> getDataLocations(ModelAlternative ma, int i) {
        ForecastGeneratorAlternative alt = getAlt(ma);
        if(alt==null)return null;
        if(DataLocation.INPUT_LOCATIONS == i){
            //input
            return alt.getInputDataLocations();
        }else{
            //ouput
            return alt.getOutputDataLocations();
        }
    }
    @Override
    public boolean setDataLocations(ModelAlternative ma, List<DataLocation> list) throws ModelLinkingException {
        ForecastGeneratorAlternative alt = getAlt(ma);
        if(alt!=null){
            return alt.setDataLocations(list);
        }
        return true;
    }
    @Override
    public boolean compute(ModelAlternative ma) {
        ForecastGeneratorAlternative alt = getAlt(ma);
        if(alt!=null){
            alt.setComputeOptions(ma.getComputeOptions());
            return alt.compute();
        }
        return false;
    }
    @Override
    public void editAlternative(ForecastGeneratorAlternative e) {
        //no editor currently
    }
    @Override
    public boolean displayApplication() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public List<GraphicElement> getGraphicElements(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public List<OutputElement> getOutputReports(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean displayEditor(GraphicElement ge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public boolean displayOutput(OutputElement oe, List<ModelAlternative> list) throws OutputException {
        return false;//no editor currently.
    }
    @Override
    public List<EditAction> getEditActions(ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void editAction(String string, ModelAlternative ma) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OutputVariable> getAvailOutputVariables(ModelAlternative ma) {
        List<OutputVariable> ret = new ArrayList<>();
        OutputVariableImpl output = new OutputVariableImpl();
        output.setName(ma._name);
        output.setParamId(Parameter.PARAMID_SPEED);//why not...
        output.setDescription("Basic Plugin output for " + ma._name);
        ret.add(output);
        return ret;
    }

    @Override
    public boolean computeOutputVariables(List<OutputVariable> list, ModelAlternative ma) {
        WatFrame fr = hec2.wat.WAT.getWatFrame();
        for(OutputVariable o : list){
            OutputVariableImpl oimpl = (OutputVariableImpl)o;
            ForecastGeneratorAlternative alt = getAlt(ma);
            fr.addMessage("Output Variable was set to NaN!!! Come fix the Forecast Generator Plugin ComputeOutputVariables method.");
            oimpl.setValue(Double.NaN);
        }
        return true;
    }

    @Override
    public boolean hasOutputVariables(ModelAlternative ma) {
        return true;
    }

}
