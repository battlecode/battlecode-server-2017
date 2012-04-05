package battlecode.engine.instrumenter;

import org.objectweb.asm.signature.SignatureWriter;

class BattlecodeSignatureWriter extends SignatureWriter {

    String teamPackageName;
    boolean silenced;
    boolean checkDisallowed;

    public BattlecodeSignatureWriter(String teamPackageName, boolean silenced, boolean checkDisallowed) {
        this.teamPackageName = teamPackageName;
        this.silenced = silenced;
        this.checkDisallowed = checkDisallowed;
    }

    public void visitClassType(String name) {
        super.visitClassType(ClassReferenceUtil.classReference(name, teamPackageName, silenced, checkDisallowed));
    }

    //public void visitTypeVariable(String name) {
    //	System.out.println("tv "+name);
    //	super.visitTypeVariable(name);
    //}

    //public void visitFormalTypeParameter(String name) {
    //	System.out.println("ftp "+name);
    //	super.visitTypeVariable(name);
    //}

    //public void visitInnerClassType(String name) {
    //	System.out.println("ict "+name);
    //	super.visitInnerClassType(ClassReferenceUtil.classReference(name,teamPackageName,silenced,checkDisallowed));
    //	super.visitInnerClassType(name);
    //}
}
