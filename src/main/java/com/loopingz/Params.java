package com.loopingz;

enum Params {

    PORT("p", "port"),
    BIND_ADDRESS("b", "bindAddress"),

    //ses configs
    REGION("r", "region"),
    CONFIGURATION("c", "configuration"),
    SOURCE_ARN("a", "sourceArn"),
    FROM_ARN("f", "fromArn"),
    RETURN_PATH_ARN("t", "returnPathArn"),

    //ssm configs
    SSM_ENABLE("ssm", "ssmEnable"), //use SSM for values
    SSM_PREFIX("ssmP", "ssmPrefix"),
    SSM_REFRESH("ssmR", "ssmRefresh"),

    //smtp configs
    SMTP_OVERRIDE("smtpO", "smtpOverride"), //use smtp instead of ses
    SMTP_HOST("smtpH", "smtpHost"),
    SMTP_PORT("smtpP", "smtpPort"),
    SMTP_USERNAME("smtpU", "smtpUsername"),
    SMTP_PASSWORD("smtpW", "smtpPassword");

    private String key;
    private String nameName;

    Params(String key, String nameName) {
        this.key = key;
        this.nameName = nameName;
    }

    public String key() {
        return key;
    }

    @Override
    public String toString() {
        return nameName;
    }
}
