package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;


public class LineCoverageTransformer implements ClassFileTransformer {

  private final String instrumentationTarget;

  public LineCoverageTransformer(String pInstrumentationTarget) {
    instrumentationTarget = pInstrumentationTarget.replace('.', '/');
  }

  @Override
  public byte[] transform(
      ClassLoader pClassLoader,
      String pClassName,
      Class<?> pClassBeingRedefined,
      ProtectionDomain pProtectionDomain,
      byte[] pClassFileBuffer) {
    if (isIgnored(pClassName)) {
      return pClassFileBuffer;
    }
    ClassReader reader = new ClassReader(pClassFileBuffer);
    //help of an llm
    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | COMPUTE_MAXS);
    ClassVisitor visitor = new InstrumentationAdapter(Opcodes.ASM9, writer);
    reader.accept(visitor, 0);
    return writer.toByteArray();
  }

  private boolean isIgnored(String pClassName) {
    return !pClassName.startsWith(instrumentationTarget) || pClassName.endsWith("Test");
  }
}
