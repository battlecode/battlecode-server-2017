package battlecode.instrumenter.bytecode;

import org.objectweb.asm.signature.SignatureWriter;

class BattlecodeSignatureWriter extends SignatureWriter {

    String teamPackageName;
    boolean checkDisallowed;

    public BattlecodeSignatureWriter(String teamPackageName, boolean checkDisallowed) {
        this.teamPackageName = teamPackageName;
        this.checkDisallowed = checkDisallowed;
    }

    public void visitClassType(String name) {
        super.visitClassType(ClassReferenceUtil.classReference(name, teamPackageName, checkDisallowed));
    }

}
