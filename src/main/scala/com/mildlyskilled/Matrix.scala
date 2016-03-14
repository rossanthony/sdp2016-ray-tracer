package com.mildlyskilled

case class Matrix(row0: Vector, row1: Vector, row2: Vector) {
  def col0 = Vector(row0.x, row1.x, row2.x)

  def col1 = Vector(row0.y, row1.y, row2.y)

  def col2 = Vector(row0.z, row1.z, row2.z)

  def *(v: Vector) = Vector(row0 dot v, row1 dot v, row2 dot v)

  def +(M: Matrix) =
    Matrix(row0 + M.row0, row1 + M.row1, row2 + M.row2)

  def *(M: Matrix) =
    Matrix(Vector(row0 dot M.col0,
      row0 dot M.col1,
      row0 dot M.col2),
      Vector(row1 dot M.col0,
        row1 dot M.col1,
        row1 dot M.col2),
      Vector(row2 dot M.col0,
        row2 dot M.col1,
        row2 dot M.col2))
}

