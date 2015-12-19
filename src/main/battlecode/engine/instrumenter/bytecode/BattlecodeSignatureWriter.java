package battlecode.engine.instrumenter.bytecode;

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

}
