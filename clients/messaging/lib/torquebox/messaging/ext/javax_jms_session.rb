require 'torquebox/messaging/javax_jms_text_message'

module javax.jms::Session

  attr_accessor :connection
  attr_accessor :naming_context

  def publish(destination_name, message)
    destination = lookup_destination( destination_name )
    producer = createProducer( destination )
    jms_message = create_text_message
    jms_message.encode message
    producer.send( jms_message )
    producer.close
  end

  # Returns decoded message, by default.  Pass :decode=>false to
  # return the original JMS TextMessage.  Pass :timeout to give up
  # after a number of milliseconds
  def receive(destination_name, options={})
    decode = options.fetch(:decode, true)
    timeout = options.fetch(:timeout, 0)
    destination = lookup_destination( destination_name )
    consumer = createConsumer( destination )
    jms_message = consumer.receive( timeout )
    if jms_message
      decode ? jms_message.decode : jms_message
    end
  end

  def lookup_destination(destination_name)
    @naming_context[ destination_name ]
  end

end

