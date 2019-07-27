# PFTradingSystem-lmax-connector
PFG trading system , Exchange=LMAX
mvn clean compile package spring-boot:repackage
Sending BBO to client example:
    while(true) {
            RelationWSController.SendBBOPointMessage(new BBOMessage());
            try{
            Thread.sleep(1000);}catch (Exception e){}
            break;
        }
Loaded beans example:
        for (String bean : beans) {
            //System.out.println("Bean :"+bean);
        }

