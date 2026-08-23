@formatter:off

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

### Excepciones generales

Se agregó un Global Exception Handler, este captura las distintas excepciones lanzadas en los servicios y 
directamente envía el mensaje declarado en cada excepción al cliente.
Se crearon excepciones generales permitiendo pasarles el mensaje de error para que sean reutilizables en distintos contextos.

### Creación de Mappers

Se crean mappers separados de los DTO, para seguir cumpliendo con el principio de responsabilidad única.
Estos mappers tienen la anotación @Component para poder ser reconocidos e inyectados por Spring.

### Uso de EntityManager para proxys

Esta clase nos permite crear proxys de las entidades. 
Estos van a ser objetos que solo existen en memoria. Los cuales nos van a permitir crear las relaciones entre 
entidades ahorrandonos consultas a la base de datos.
- Por ejemplo en el ProductCreatorService se usa uno de estos proxys para la relación del producto con el comercio. De 
esta forma solo con el id podemos establecer la relación.
En este caso es usable debido a que el comercio va a estar validado ya que llega desde el jwt token previamente validado por el filtro de seguridad.
