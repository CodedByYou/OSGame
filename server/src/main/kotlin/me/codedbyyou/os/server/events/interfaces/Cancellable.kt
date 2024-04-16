package me.codedbyyou.os.server.events.interfaces

/**
 * self-explanatory
 * @author abdollah kandrani
 * @version 1.0
 * @since 1.0
 * Date: 9/11/2022
 */
interface Cancellable {
    /**
     * @return whether the event is cancelled or not
     * This is a getter for the isCancelled variable and setter for the isCancelled variable
     * setter can be overridden to add some logic, courtesy of kotlin
     * @link https://kotlinlang.org/docs/properties.html#backing-properties
     */
     var isCancelled : Boolean
}