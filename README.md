# Sistema de gestion de stock y control de ventas

Este es un proyecto realizado para la materia programación IV de la Tecnicatura Universitaria en Programación (TUP),
UTN.

## Explicación de ciertas elecciones

### Uso de Records para la creación de los DTO (Data Transfer Object)

Se utilizan Records debido a simplicidad a la hora de crearlos, estos por defecto ya traen metodos utiles sin 
necesidad de crearlos a mano. Además estos cuentan con la cualidad de ser inmutables por lo que nos asegura mayor 
consistencia en los datos.
*** Cosas a saber ***
- Los atributos por defecto son final, por lo que no se pueden modificar una vez creados.
- La forma de acceder a los atributos es mediante metodos. Pero no con el prefijo get, sino con el nombre del 
  atributo. Por ejemplo mirecord.nombre() para acceder al atributo nombre.