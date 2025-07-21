package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;


class InstrumentationAdapter extends ClassVisitor {

  InstrumentationAdapter(int pAPI, ClassWriter pClassWriter) {
    super(pAPI, pClassWriter);
  }

  @Override
  public MethodVisitor visitMethod(
      int pAccess, String pName, String pDescriptor, String pSignature, String[] pExceptions) {
    MethodVisitor mv = super.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);
    return new MethodVisitor(api, mv) {
      @Override
      public void visitLineNumber(int pLine, Label pStart) {
        super.visitLineNumber(pLine, pStart);
        visitLdcInsn(pLine);
        visitMethodInsn(
                INVOKESTATIC,
                "de/uni_passau/fim/se2/sa/slicing/coverage/CoverageTracker",
                "trackLineVisit",
                "(I)V",
                false
        );
      }
    };
  }
}
