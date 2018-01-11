package consulting.vectr.model

case class DekamerQRVAResponse(
                                disclaimer: String,
                                start: Int,
                                total: Int,
                                items: List[DekamerQRVAResponseItem]
                              )
