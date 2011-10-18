/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.sip.dtmf;

import gov.nist.javax.sip.header.*;

import java.text.*;
import java.util.*;

import javax.sip.*;
import javax.sip.header.*;
import javax.sip.message.*;

import net.java.sip.communicator.impl.protocol.sip.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * Sending DTMFs with SIP INFO.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 */
public class DTMFInfo
    extends MethodProcessorAdapter
{
    /**
     * The <tt>Logger</tt> used by the <tt>DTMFInfo</tt> class and its instances
     * for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(DTMFInfo.class);

    /**
     * The sub-type of the content of the <tt>Request</tt>s being sent by
     * <tt>DTMFInfo</tt>.
     */
    private static final String CONTENT_SUB_TYPE = "dtmf-relay";

    /**
     * The type of the content of the <tt>Request</tt>s being sent by
     * <tt>DTMFInfo</tt>.
     */
    private static final String CONTENT_TYPE = "application";

    /**
     * Maps call peers and tone and its start time, so we can compute duration.
     */
    private Hashtable<CallPeer, Object[]>
        currentlyTransmittingTones = new Hashtable<CallPeer, Object[]>();

    /**
     * Involved protocol provider service.
     */
    private final ProtocolProviderServiceSipImpl pps;

    /**
     * Constructor
     *
     * @param pps the SIP Protocol provider service
     */
    public DTMFInfo(ProtocolProviderServiceSipImpl pps)
    {
        this.pps = pps;

        this.pps.registerMethodProcessor(Request.INFO, this);
    }

    /**
     * Saves the tone we need to send and its start time. With start time we
     * can compute the duration later when we need to send the DTMF.
     * 
     * @param callPeer the call peer.
     * @param tone the tone to transmit.
     * @throws OperationFailedException
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public void startSendingDTMF(CallPeerSipImpl callPeer, DTMFTone tone)
        throws OperationFailedException,
                NullPointerException,
                IllegalArgumentException
    {
        if(currentlyTransmittingTones.contains(callPeer))
            throw new IllegalStateException(
                "Error starting dtmf tone, already started");

        currentlyTransmittingTones.put(callPeer,
            new Object[]{tone, System.currentTimeMillis()});
    }

    /**
     * Sending of the currently saved tone.
     * @param callPeer
     */
    public void stopSendingDTMF(CallPeerSipImpl callPeer)
    {
        Object[] toneInfo =
            currentlyTransmittingTones.remove(callPeer);

        if(toneInfo != null)
        {
            try
            {
                long startTime = (Long)toneInfo[1];
                sayInfo(callPeer,
                    (DTMFTone) toneInfo[0],
                     System.currentTimeMillis() - startTime);
            } catch (OperationFailedException ex)
            {
                logger.error("Error stoping dtmf ");
            }
        }
    }

    /**
     * This is just a copy of the bye method from the OpSetBasicTelephony,
     * which was enhanced with a body in order to send the DTMF tone
     *
     * @param callPeer destination of the DTMF tone
     * @param dtmftone DTMF tone to send
     * @param duration the duration of the tone
     * @throws OperationFailedException
     */
    private void sayInfo(CallPeerSipImpl callPeer,
                         DTMFTone dtmftone, long duration)
        throws OperationFailedException
    {
        Request info = pps.getMessageFactory().createRequest(
                        callPeer.getDialog(), Request.INFO);

        //here we add the body
        ContentType ct = new ContentType(CONTENT_TYPE, CONTENT_SUB_TYPE);
        String content
            = "Signal=" + dtmftone.getValue()
                + "\r\nDuration=" + duration + "\r\n";

        ContentLength cl = new ContentLength(content.length());
        info.setContentLength(cl);

        try
        {
            info.setContent(content.getBytes(), ct);
        }
        catch (ParseException ex)
        {
            logger.error("Failed to construct the INFO request", ex);
            throw new OperationFailedException(
                "Failed to construct a client the INFO request"
                , OperationFailedException.INTERNAL_ERROR
                , ex);

        }
        //body ended
        ClientTransaction clientTransaction = null;
        try
        {
            clientTransaction = callPeer.getJainSipProvider()
                .getNewClientTransaction(info);
        }
        catch (TransactionUnavailableException ex)
        {
            logger.error(
                "Failed to construct a client transaction from the INFO request"
                , ex);
            throw new OperationFailedException(
                "Failed to construct a client transaction from the INFO request"
                , OperationFailedException.INTERNAL_ERROR
                , ex);
        }

        try
        {
            if (callPeer.getDialog().getState()
                == DialogState.TERMINATED)
            {
                //this is probably because the call has just ended, so don't
                //throw an exception. simply log and get lost.
                logger.warn("Trying to send a dtmf tone inside a "
                            +"TERMINATED dialog.");
                return;
            }

            callPeer.getDialog().sendRequest(clientTransaction);
            if (logger.isDebugEnabled())
                logger.debug("sent request:\n" + info);
        }
        catch (SipException ex)
        {
            throw new OperationFailedException(
                "Failed to send the INFO request"
                , OperationFailedException.NETWORK_FAILURE
                , ex);
        }
    }

    /**
     * Just look if the DTMF signal was well received, and log it
     *
     * @param responseEvent the response event
     * @return <tt>true</tt> if the specified event has been handled by this
     *         processor and shouldn't be offered to other processors registered
     *         for the same method; <tt>false</tt>, otherwise
     */
    @Override
    public boolean processResponse(ResponseEvent responseEvent)
    {
        boolean processed = false;

        if (responseEvent == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("null responseEvent");
        }
        else
        {
            Response response = responseEvent.getResponse();

            if (response == null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("null response");
            }
            else
            {
                // Is it even for us?
                ClientTransaction clientTransaction
                    = responseEvent.getClientTransaction();

                if (clientTransaction == null)
                {
                    if (logger.isDebugEnabled())
                        logger.debug("null clientTransaction");
                }
                else
                {
                    Request request = clientTransaction.getRequest();

                    if (request == null)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("null request");
                    }
                    else
                    {
                        ContentTypeHeader contentTypeHeader
                            = (ContentTypeHeader)
                                request.getHeader(ContentTypeHeader.NAME);

                        if ((contentTypeHeader != null)
                                && CONTENT_TYPE.equalsIgnoreCase(
                                        contentTypeHeader.getContentType())
                                && CONTENT_SUB_TYPE.equalsIgnoreCase(
                                        contentTypeHeader.getContentSubType()))
                        {
                            processed = true;

                            int statusCode = response.getStatusCode();

                            if (statusCode == 200)
                            {
                                if (logger.isDebugEnabled())   
                                    logger.debug(
                                            "DTMF send succeeded: "
                                                + statusCode);
                            }
                            else
                                logger.error("DTMF send failed: " + statusCode);
                        }
                    }
                }
            }
        }
        return processed;
    }
}
