package com.bili.domain;

public class JsonResponse<T> {
   private String code;
   private String msg;
   private T data;

   public JsonResponse(String code, String msg){
       this.code =code;
       this.msg = msg;
   }

   public JsonResponse(T data){
       this.data = data;
       this.msg = "success";
       this.code = "0";
   }

   public static JsonResponse<String> success(){
       return new JsonResponse<>(null);
   }

   public static JsonResponse<String> success(String data){
       return new JsonResponse<>(data);
   }

   public static JsonResponse<String> failed(){
       return new JsonResponse<>("1", "failed");
   }

   public static JsonResponse<String> failed(String code, String msg){
       return new JsonResponse<>(code ,msg);
   }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }
}
