package com.linkedin.thirdeye.hadoop.topk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Wrapper for the key generated by mapper in TopKPhase
 */
public class TopKPhaseMapOutputKey {

  String dimensionName;
  String dimensionValue;

  public TopKPhaseMapOutputKey(String dimensionName, String dimensionValue) {
    this.dimensionName = dimensionName;
    this.dimensionValue = dimensionValue;
  }

  public String getDimensionName() {
    return dimensionName;
  }

  public String getDimensionValue() {
    return dimensionValue;
  }

  public byte[] toBytes() throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    byte[] bytes;
    // dimension name
    bytes = dimensionName.getBytes();
    dos.writeInt(bytes.length);
    dos.write(bytes);
    // dimension value
    bytes = dimensionValue.getBytes();
    dos.writeInt(bytes.length);
    dos.write(bytes);

    baos.close();
    dos.close();
    return baos.toByteArray();
  }

  public static TopKPhaseMapOutputKey fromBytes(byte[] buffer) throws IOException {
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buffer));
    int length;
    byte[] bytes;
    // dimension name
    length = dis.readInt();
    bytes = new byte[length];
    dis.read(bytes);
    String dimensionName = new String(bytes);
    // dimension value
    length = dis.readInt();
    bytes = new byte[length];
    dis.read(bytes);
    String dimensionValue = new String(bytes);

    TopKPhaseMapOutputKey wrapper;
    wrapper = new TopKPhaseMapOutputKey(dimensionName, dimensionValue);
    return wrapper;
  }

}
