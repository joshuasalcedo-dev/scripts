package io.joshuasalcedo.script.model;

public enum ScriptLanguage {
    JAVASCRIPT("JavaScript", "js", "text/javascript"),
    PYTHON("jython", "py", "text/x-python"),
    GROOVY("groovy", "groovy", "text/x-groovy"),
    RUBY("jruby", "rb", "text/x-ruby");
    
    private final String engineName;
    private final String fileExtension;
    private final String mimeType;
    
    ScriptLanguage(String engineName, String fileExtension, String mimeType) {
        this.engineName = engineName;
        this.fileExtension = fileExtension;
        this.mimeType = mimeType;
    }
    
    public String getEngineName() {
        return engineName;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public javax.script.ScriptEngine getEngine() {
        return new javax.script.ScriptEngineManager().getEngineByName(engineName);
    }
}